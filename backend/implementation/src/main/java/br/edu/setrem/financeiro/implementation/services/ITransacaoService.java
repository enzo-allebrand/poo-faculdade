package br.edu.setrem.financeiro.implementation.services;

import br.edu.setrem.financeiro.implementation.models.TransacaoKafkaEvent;
import br.edu.setrem.financeiro.implementation.models.TransacaoModels;

import java.util.List;

/**
 * Contrato de gerenciamento de transações, filtros e transferências via mensageria.
 */
public interface ITransacaoService {
    List<TransacaoModels.TransacaoResponseDTO> listarFiltradas(Long usuarioId, TransacaoModels.TransacaoFiltro filtro);
    TransacaoModels.EventoEnfileiradoDTO solicitarLancamento(Long usuarioId, TransacaoModels.TransacaoRequest request);
    TransacaoModels.EventoEnfileiradoDTO solicitarTransferencia(Long usuarioId, TransacaoModels.TransferenciaDTO request);
    TransacaoModels.TransacaoResponseDTO atualizar(Long usuarioId, Long transacaoId, TransacaoModels.TransacaoRequest request);
    void excluir(Long usuarioId, Long transacaoId, boolean excluirPar);
    void processarEvento(TransacaoKafkaEvent evento);
}
