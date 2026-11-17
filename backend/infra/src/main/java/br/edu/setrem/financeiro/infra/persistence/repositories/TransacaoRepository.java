package br.edu.setrem.financeiro.infra.persistence.repositories;

import br.edu.setrem.financeiro.domain.entities.Transacao;
import br.edu.setrem.financeiro.implementation.models.TransacaoModels;
import br.edu.setrem.financeiro.implementation.repositories.ITransacaoRepository;
import br.edu.setrem.financeiro.infra.persistence.entities.CategoriaEntity;
import br.edu.setrem.financeiro.infra.persistence.entities.ContaEntity;
import br.edu.setrem.financeiro.infra.persistence.entities.TransacaoEntity;
import br.edu.setrem.financeiro.infra.persistence.entities.UsuarioEntity;
import br.edu.setrem.financeiro.infra.persistence.mappers.PersistenceMapper;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Repositório Panache otimizado com JOIN FETCH e índices para leitura e gravação em lote.
 */
@ApplicationScoped
public class TransacaoRepository implements PanacheRepository<TransacaoEntity>, ITransacaoRepository {

    @Override
    public List<Transacao> listarPorUsuario(Long usuarioId) {
        return getEntityManager()
                .createQuery("select t from TransacaoEntity t join fetch t.conta join fetch t.categoria where t.usuario.id = :usuarioId", TransacaoEntity.class)
                .setParameter("usuarioId", usuarioId)
                .getResultList()
                .stream()
                .map(PersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<Transacao> listarUltimasPorUsuario(Long usuarioId, int limite) {
        return getEntityManager()
                .createQuery("select t from TransacaoEntity t join fetch t.conta join fetch t.categoria where t.usuario.id = :usuarioId order by t.datatransacao desc, t.id desc", TransacaoEntity.class)
                .setParameter("usuarioId", usuarioId)
                .setMaxResults(limite)
                .getResultList()
                .stream()
                .map(PersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<Transacao> listarFiltradas(Long usuarioId, TransacaoModels.TransacaoFiltro filtro, LocalDateTime inicio, LocalDateTime fim) {
        StringBuilder jpql = new StringBuilder("select t from TransacaoEntity t join fetch t.conta join fetch t.categoria where t.usuario.id = :usuarioId");
        Map<String, Object> params = new HashMap<>();
        params.put("usuarioId", usuarioId);

        if (filtro.contaId() != null) {
            jpql.append(" and t.conta.id = :contaId");
            params.put("contaId", filtro.contaId());
        }
        if (filtro.categoriaId() != null) {
            jpql.append(" and t.categoria.id = :categoriaId");
            params.put("categoriaId", filtro.categoriaId());
        }
        if (filtro.tipo() != null && !filtro.tipo().isBlank()) {
            jpql.append(" and t.tipo = :tipo");
            params.put("tipo", filtro.tipo().trim().toUpperCase());
        }
        if (filtro.busca() != null && !filtro.busca().isBlank()) {
            jpql.append(" and lower(t.descricao) like :busca");
            params.put("busca", "%" + filtro.busca().trim().toLowerCase() + "%");
        }
        if (inicio != null) {
            jpql.append(" and t.datatransacao >= :inicio");
            params.put("inicio", inicio);
        }
        if (fim != null) {
            jpql.append(" and t.datatransacao <= :fim");
            params.put("fim", fim);
        }

        jpql.append(" order by t.datatransacao desc, t.id desc");

        TypedQuery<TransacaoEntity> query = getEntityManager().createQuery(jpql.toString(), TransacaoEntity.class);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }

        return query.getResultList().stream().map(PersistenceMapper::toDomain).toList();
    }

    @Override
    public Optional<Transacao> buscarPorIdEUsuario(Long id, Long usuarioId) {
        return find("id = ?1 and usuario.id = ?2", id, usuarioId)
                .firstResultOptional()
                .map(PersistenceMapper::toDomain);
    }

    @Override
    public long contarPorConta(Long contaId, Long usuarioId) {
        return count("conta.id = ?1 and usuario.id = ?2", contaId, usuarioId);
    }

    @Override
    public long contarPorCategoria(Long categoriaId, Long usuarioId) {
        return count("categoria.id = ?1 and usuario.id = ?2", categoriaId, usuarioId);
    }

    @Override
    public boolean existePorCorrelationId(String correlationId) {
        return count("correlationId", correlationId) > 0;
    }

    @Override
    public Transacao salvar(Transacao transacao, String correlationId) {
        TransacaoEntity entity = toEntity(transacao, correlationId);
        persist(entity);
        getEntityManager().flush();
        return find("id = ?1", entity.id).firstResultOptional()
                .map(PersistenceMapper::toDomain)
                .orElseGet(() -> PersistenceMapper.toDomain(entity));
    }

    @Override
    public void salvarEmLote(List<Transacao> transacoes, String correlationId) {
        for (Transacao t : transacoes) {
            persist(toEntity(t, correlationId));
        }
    }

    @Override
    public void excluir(Long id, Long usuarioId) {
        delete("id = ?1 and usuario.id = ?2", id, usuarioId);
    }

    @Override
    public void excluirPorTransferencia(String transferenciaId, Long usuarioId) {
        delete("transferenciaId = ?1 and usuario.id = ?2", transferenciaId, usuarioId);
    }

    private TransacaoEntity toEntity(Transacao t, String correlationId) {
        TransacaoEntity entity = t.getId() != null ? findById(t.getId()) : new TransacaoEntity();
        if (entity == null) {
            entity = new TransacaoEntity();
        }
        entity.descricao = t.getDescricao();
        entity.valor = t.getValor();
        entity.tipo = t.getTipo().name();
        entity.datatransacao = t.getDatatransacao();
        entity.transferenciaId = t.getTransferenciaId();
        if (correlationId != null) {
            entity.correlationId = correlationId;
        }
        entity.conta = getEntityManager().getReference(ContaEntity.class, t.getContaId());
        entity.categoria = getEntityManager().getReference(CategoriaEntity.class, t.getCategoriaId());
        entity.usuario = getEntityManager().getReference(UsuarioEntity.class, t.getUsuarioId());
        return entity;
    }
}
