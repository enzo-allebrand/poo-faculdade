package br.edu.setrem.financeiro.implementation.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Modelos e contratos de entrada/saída para contas financeiras.
 */
public final class ContaModels {

    private ContaModels() {
    }

    public record ContaRequest(
            @NotBlank(message = "Descrição da conta é obrigatória")
            @Size(max = 150, message = "Descrição deve ter no máximo 150 caracteres")
            String descricao,

            BigDecimal saldoInicial
    ) {
    }

    public record ContaSaldoDTO(
            Long id,
            String descricao,
            BigDecimal saldoInicial,
            BigDecimal totalReceitas,
            BigDecimal totalDespesas,
            BigDecimal saldoAtual
    ) {
    }
}
