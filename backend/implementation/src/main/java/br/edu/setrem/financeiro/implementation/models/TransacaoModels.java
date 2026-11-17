package br.edu.setrem.financeiro.implementation.models;

import br.edu.setrem.financeiro.domain.entities.Transacao;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Modelos de entrada, filtro, saída e enfileiramento assíncrono para transações.
 */
public final class TransacaoModels {

    private TransacaoModels() {
    }

    public record TransacaoRequest(
            @NotBlank(message = "Descrição é obrigatória")
            @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
            String descricao,

            @NotNull(message = "Valor é obrigatório")
            @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
            BigDecimal valor,

            @NotBlank(message = "Tipo é obrigatório")
            @Pattern(regexp = "^[RD]$", message = "Tipo deve ser 'R' (Receita) ou 'D' (Despesa)")
            String tipo,

            LocalDateTime datatransacao,

            @NotNull(message = "Conta é obrigatória")
            Long contaId,

            @NotNull(message = "Categoria é obrigatória")
            Long categoriaId
    ) {
    }

    public record TransacaoFiltro(
            Long contaId,
            Long categoriaId,
            String tipo,
            String dataInicio,
            String dataFim,
            String busca
    ) {
    }

    public record TransacaoResponseDTO(
            Long id,
            String descricao,
            BigDecimal valor,
            String tipo,
            LocalDateTime datatransacao,
            String transferenciaId,
            Long contaId,
            String contaDescricao,
            Long categoriaId,
            String categoriaDescricao
    ) {
        public static TransacaoResponseDTO from(Transacao t) {
            return new TransacaoResponseDTO(
                    t.getId(),
                    t.getDescricao(),
                    t.getValor(),
                    t.getTipo().name(),
                    t.getDatatransacao(),
                    t.getTransferenciaId(),
                    t.getContaId(),
                    t.getContaDescricao(),
                    t.getCategoriaId(),
                    t.getCategoriaDescricao()
            );
        }
    }

    public record TransferenciaDTO(
            @NotNull(message = "Conta de origem é obrigatória")
            Long contaOrigemId,

            @NotNull(message = "Conta de destino é obrigatória")
            Long contaDestinoId,

            Long categoriaId,

            @NotNull(message = "Valor é obrigatório")
            @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
            BigDecimal valor,

            @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
            String descricao,

            LocalDateTime datatransacao
    ) {
    }

    public record EventoEnfileiradoDTO(
            String status,
            String correlationId,
            String message
    ) {
    }
}
