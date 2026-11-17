package br.edu.setrem.financeiro.implementation.repositories;

import br.edu.setrem.financeiro.domain.entities.Usuario;

import java.util.Optional;

/**
 * Contrato de persistência para usuários.
 */
public interface IUsuarioRepository {
    Optional<Usuario> buscarPorId(Long id);
    Optional<Usuario> buscarPorEmail(String email);
    Usuario salvar(Usuario usuario);
}
