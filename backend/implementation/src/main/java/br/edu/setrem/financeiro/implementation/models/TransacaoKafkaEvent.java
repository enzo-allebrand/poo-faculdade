package br.edu.setrem.financeiro.implementation.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Evento enviado ao tópico Kafka para processamento assíncrono de lançamentos e transferências.
 */
public record TransacaoKafkaEvent(
        String correlationId,
        String operacao,
        Long usuarioId,
        String descricao,
        BigDecimal valor,
        String tipo,
        LocalDateTime datatransacao,
        Long contaId,
        Long categoriaId,
        Long contaOrigemId,
        Long contaDestinoId,
        String transferenciaId
) {
    public static final String OPERACAO_LANCAMENTO = "LANCAMENTO";
    public static final String OPERACAO_TRANSFERENCIA = "TRANSFERENCIA";
}
