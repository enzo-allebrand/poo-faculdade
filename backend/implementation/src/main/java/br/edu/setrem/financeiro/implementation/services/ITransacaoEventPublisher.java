package br.edu.setrem.financeiro.implementation.services;

import br.edu.setrem.financeiro.implementation.models.TransacaoKafkaEvent;

/**
 * Publicador de eventos de transações para fila assíncrona (Kafka).
 */
public interface ITransacaoEventPublisher {
    void publicar(TransacaoKafkaEvent evento);
}
