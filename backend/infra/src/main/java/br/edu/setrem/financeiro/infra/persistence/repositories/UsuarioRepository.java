package br.edu.setrem.financeiro.infra.persistence.repositories;

import br.edu.setrem.financeiro.domain.entities.Usuario;
import br.edu.setrem.financeiro.implementation.repositories.IUsuarioRepository;
import br.edu.setrem.financeiro.infra.persistence.entities.UsuarioEntity;
import br.edu.setrem.financeiro.infra.persistence.mappers.PersistenceMapper;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

/**
 * Repositório Panache para persistência de usuários.
 */
@ApplicationScoped
public class UsuarioRepository implements PanacheRepository<UsuarioEntity>, IUsuarioRepository {

    @Override
    public Optional<Usuario> buscarPorId(Long id) {
        return findByIdOptional(id).map(PersistenceMapper::toDomain);
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        return find("email", email).firstResultOptional().map(PersistenceMapper::toDomain);
    }

    @Override
    public Usuario salvar(Usuario usuario) {
        UsuarioEntity entity = usuario.getId() != null ? findById(usuario.getId()) : new UsuarioEntity();
        if (entity == null) {
            entity = new UsuarioEntity();
        }
        entity.nome = usuario.getNome();
        entity.email = usuario.getEmail();
        entity.senhaHash = usuario.getSenhaHash();
        persist(entity);
        return PersistenceMapper.toDomain(entity);
    }
}
