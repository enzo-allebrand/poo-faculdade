package br.edu.setrem.financeiro.infra.messaging;

import br.edu.setrem.financeiro.implementation.models.TransacaoKafkaEvent;
import br.edu.setrem.financeiro.implementation.services.ITransacaoEventPublisher;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.OnOverflow;

/**
 * Produtor Kafka para enfileiramento de lançamentos e transferências sem sobrecarregar o banco.
 */
@ApplicationScoped
public class KafkaTransacaoEventPublisher implements ITransacaoEventPublisher {

    @Inject
    @Channel("transacoes-out")
    @OnOverflow(value = OnOverflow.Strategy.BUFFER, bufferSize = 10000)
    Emitter<String> emitter;

    @Inject
    ObjectMapper objectMapper;

    @Override
    public void publicar(TransacaoKafkaEvent evento) {
        try {
            String payload = objectMapper.writeValueAsString(evento);
            emitter.send(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Falha ao serializar evento de transação para o Kafka.", e);
        }
    }
}
