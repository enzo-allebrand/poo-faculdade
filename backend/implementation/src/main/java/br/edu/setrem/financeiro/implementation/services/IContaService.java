package br.edu.setrem.financeiro.implementation.services;

import br.edu.setrem.financeiro.implementation.models.ContaModels;

import java.util.List;

/**
 * Contrato de gerenciamento de contas financeiras.
 */
public interface IContaService {
    List<ContaModels.ContaSaldoDTO> listarComSaldos(Long usuarioId);
    ContaModels.ContaSaldoDTO criar(Long usuarioId, ContaModels.ContaRequest request);
    ContaModels.ContaSaldoDTO atualizar(Long usuarioId, Long contaId, ContaModels.ContaRequest request);
    void excluir(Long usuarioId, Long contaId);
}
