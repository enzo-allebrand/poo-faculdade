package br.edu.setrem.financeiro.infra.persistence.repositories;

import br.edu.setrem.financeiro.domain.entities.Conta;
import br.edu.setrem.financeiro.implementation.repositories.IContaRepository;
import br.edu.setrem.financeiro.infra.persistence.entities.ContaEntity;
import br.edu.setrem.financeiro.infra.persistence.entities.UsuarioEntity;
import br.edu.setrem.financeiro.infra.persistence.mappers.PersistenceMapper;
import io.quarkus.cache.CacheInvalidateAll;
import io.quarkus.cache.CacheResult;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

/**
 * Repositório Panache com cache para validação rápida de propriedade de contas sob alta carga.
 */
@ApplicationScoped
public class ContaRepository implements PanacheRepository<ContaEntity>, IContaRepository {

    @Override
    public List<Conta> listarPorUsuario(Long usuarioId) {
        return list("usuario.id = ?1 order by id asc", usuarioId)
                .stream()
                .map(PersistenceMapper::toDomain)
                .toList();
    }

    @Override
    @CacheResult(cacheName = "conta-cache")
    public Optional<Conta> buscarPorIdEUsuario(Long id, Long usuarioId) {
        return find("id = ?1 and usuario.id = ?2", id, usuarioId)
                .firstResultOptional()
                .map(PersistenceMapper::toDomain);
    }

    @Override
    @CacheInvalidateAll(cacheName = "conta-cache")
    public Conta salvar(Conta conta) {
        ContaEntity entity = conta.getId() != null ? findById(conta.getId()) : new ContaEntity();
        if (entity == null) {
            entity = new ContaEntity();
        }
        entity.descricao = conta.getDescricao();
        entity.saldoInicial = conta.getSaldoInicial();
        entity.usuario = getEntityManager().getReference(UsuarioEntity.class, conta.getUsuarioId());
        persist(entity);
        return PersistenceMapper.toDomain(entity);
    }

    @Override
    @CacheInvalidateAll(cacheName = "conta-cache")
    public void excluir(Long id, Long usuarioId) {
        delete("id = ?1 and usuario.id = ?2", id, usuarioId);
    }
}
