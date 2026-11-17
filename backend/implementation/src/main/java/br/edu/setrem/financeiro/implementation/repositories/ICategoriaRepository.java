package br.edu.setrem.financeiro.implementation.repositories;

import br.edu.setrem.financeiro.domain.entities.Categoria;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de persistência para categorias financeiras.
 */
public interface ICategoriaRepository {
    List<Categoria> listarPorUsuario(Long usuarioId);
    Optional<Categoria> buscarPorIdEUsuario(Long id, Long usuarioId);
    Optional<Categoria> buscarCategoriaTransferencia(Long usuarioId);
    Categoria salvar(Categoria categoria);
    void excluir(Long id, Long usuarioId);
}
