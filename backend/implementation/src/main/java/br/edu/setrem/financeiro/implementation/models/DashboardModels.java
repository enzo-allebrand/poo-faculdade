package br.edu.setrem.financeiro.implementation.models;

import java.math.BigDecimal;
import java.util.List;

/**
 * Resumo financeiro da tela inicial e payload padrão de erros.
 */
public final class DashboardModels {

    private DashboardModels() {
    }

    public record DashboardResumoDTO(
            BigDecimal saldoGeral,
            BigDecimal totalReceitas,
            BigDecimal totalDespesas,
            List<ContaModels.ContaSaldoDTO> contas,
            List<TransacaoModels.TransacaoResponseDTO> ultimasTransacoes
    ) {
    }

    public record ErrorResponse(String message) {
    }
}
