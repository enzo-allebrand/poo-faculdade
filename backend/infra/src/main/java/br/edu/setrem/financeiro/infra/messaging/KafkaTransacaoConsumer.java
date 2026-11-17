package br.edu.setrem.financeiro.infra.messaging;

import br.edu.setrem.financeiro.implementation.models.TransacaoKafkaEvent;
import br.edu.setrem.financeiro.implementation.services.ITransacaoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.reactive.messaging.annotations.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

/**
 * Consumidor Kafka que persiste eventos no PostgreSQL em fluxo controlado para proteger o pool de conexões.
 */
@ApplicationScoped
public class KafkaTransacaoConsumer {

    private static final Logger LOG = Logger.getLogger(KafkaTransacaoConsumer.class);

    @Inject
    ITransacaoService transacaoService;

    @Inject
    ObjectMapper objectMapper;

    @Incoming("transacoes-in")
    @Blocking
    public void consumir(String payload) {
        try {
            TransacaoKafkaEvent evento = objectMapper.readValue(payload, TransacaoKafkaEvent.class);
            transacaoService.processarEvento(evento);
        } catch (Exception e) {
            LOG.errorf(e, "Erro ao processar evento Kafka de transação: %s", payload);
        }
    }
}
