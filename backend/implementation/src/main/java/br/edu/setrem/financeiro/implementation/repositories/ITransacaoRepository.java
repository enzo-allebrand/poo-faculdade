package br.edu.setrem.financeiro.implementation.repositories;

import br.edu.setrem.financeiro.domain.entities.Transacao;
import br.edu.setrem.financeiro.implementation.models.TransacaoModels;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Contrato de persistência para transações financeiras.
 */
public interface ITransacaoRepository {
    List<Transacao> listarPorUsuario(Long usuarioId);
    List<Transacao> listarUltimasPorUsuario(Long usuarioId, int limite);
    List<Transacao> listarFiltradas(Long usuarioId, TransacaoModels.TransacaoFiltro filtro, LocalDateTime inicio, LocalDateTime fim);
    Optional<Transacao> buscarPorIdEUsuario(Long id, Long usuarioId);
    long contarPorConta(Long contaId, Long usuarioId);
    long contarPorCategoria(Long categoriaId, Long usuarioId);
    boolean existePorCorrelationId(String correlationId);
    Transacao salvar(Transacao transacao, String correlationId);
    void salvarEmLote(List<Transacao> transacoes, String correlationId);
    void excluir(Long id, Long usuarioId);
    void excluirPorTransferencia(String transferenciaId, Long usuarioId);
}
