package br.edu.setrem.financeiro.implementation.repositories;

import br.edu.setrem.financeiro.domain.entities.Conta;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de persistência para contas financeiras.
 */
public interface IContaRepository {
    List<Conta> listarPorUsuario(Long usuarioId);
    Optional<Conta> buscarPorIdEUsuario(Long id, Long usuarioId);
    Conta salvar(Conta conta);
    void excluir(Long id, Long usuarioId);
}
