package br.edu.setrem.financeiro.infra.persistence.repositories;

import br.edu.setrem.financeiro.domain.entities.Categoria;
import br.edu.setrem.financeiro.implementation.repositories.ICategoriaRepository;
import br.edu.setrem.financeiro.infra.persistence.entities.CategoriaEntity;
import br.edu.setrem.financeiro.infra.persistence.entities.UsuarioEntity;
import br.edu.setrem.financeiro.infra.persistence.mappers.PersistenceMapper;
import io.quarkus.cache.CacheInvalidateAll;
import io.quarkus.cache.CacheResult;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

/**
 * Repositório Panache com cache para categorias financeiras.
 */
@ApplicationScoped
public class CategoriaRepository implements PanacheRepository<CategoriaEntity>, ICategoriaRepository {

    @Override
    public List<Categoria> listarPorUsuario(Long usuarioId) {
        return list("usuario.id = ?1 order by id asc", usuarioId)
                .stream()
                .map(PersistenceMapper::toDomain)
                .toList();
    }

    @Override
    @CacheResult(cacheName = "categoria-cache")
    public Optional<Categoria> buscarPorIdEUsuario(Long id, Long usuarioId) {
        return find("id = ?1 and usuario.id = ?2", id, usuarioId)
                .firstResultOptional()
                .map(PersistenceMapper::toDomain);
    }

    @Override
    public Optional<Categoria> buscarCategoriaTransferencia(Long usuarioId) {
        return find("usuario.id = ?1 and lower(descricao) like 'transfer%'", usuarioId)
                .firstResultOptional()
                .map(PersistenceMapper::toDomain);
    }

    @Override
    @CacheInvalidateAll(cacheName = "categoria-cache")
    public Categoria salvar(Categoria categoria) {
        CategoriaEntity entity = categoria.getId() != null ? findById(categoria.getId()) : new CategoriaEntity();
        if (entity == null) {
            entity = new CategoriaEntity();
        }
        entity.descricao = categoria.getDescricao();
        entity.tipo = categoria.getTipo().name();
        entity.usuario = getEntityManager().getReference(UsuarioEntity.class, categoria.getUsuarioId());
        persist(entity);
        return PersistenceMapper.toDomain(entity);
    }

    @Override
    @CacheInvalidateAll(cacheName = "categoria-cache")
    public void excluir(Long id, Long usuarioId) {
        delete("id = ?1 and usuario.id = ?2", id, usuarioId);
    }
}
