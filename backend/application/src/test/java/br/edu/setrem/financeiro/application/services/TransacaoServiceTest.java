package br.edu.setrem.financeiro.application.services;

import br.edu.setrem.financeiro.domain.entities.Categoria;
import br.edu.setrem.financeiro.domain.entities.Conta;
import br.edu.setrem.financeiro.domain.entities.Transacao;
import br.edu.setrem.financeiro.domain.enums.TipoCategoriaEnum;
import br.edu.setrem.financeiro.implementation.exceptions.RegraNegocioException;
import br.edu.setrem.financeiro.implementation.models.TransacaoKafkaEvent;
import br.edu.setrem.financeiro.implementation.models.TransacaoModels;
import br.edu.setrem.financeiro.implementation.repositories.ICategoriaRepository;
import br.edu.setrem.financeiro.implementation.repositories.IContaRepository;
import br.edu.setrem.financeiro.implementation.repositories.ITransacaoRepository;
import br.edu.setrem.financeiro.implementation.services.ITransacaoEventPublisher;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários puros da camada Application sem depender de banco ou Kafka.
 */
class TransacaoServiceTest {

    @Test
    void naoPermiteTransferenciaEntreAMesmaConta() {
        TransacaoService service = criarService(new ArrayList<>(), new ArrayList<>());
        TransacaoModels.TransferenciaDTO dto = new TransacaoModels.TransferenciaDTO(
                1L, 1L, null, new BigDecimal("100.00"), "Teste", LocalDateTime.now()
        );

        assertThrows(RegraNegocioException.class, () -> service.solicitarTransferencia(10L, dto));
    }

    @Test
    void enfileiraLancamentoEProcessaEventoComIdempotencia() {
        List<TransacaoKafkaEvent> eventosPublicados = new ArrayList<>();
        List<Transacao> bancoMemoria = new ArrayList<>();
        TransacaoService service = criarService(eventosPublicados, bancoMemoria);

        TransacaoModels.EventoEnfileiradoDTO res = service.solicitarLancamento(
                10L,
                new TransacaoModels.TransacaoRequest("Almoço", new BigDecimal("45.00"), "D", LocalDateTime.now(), 1L, 2L)
        );

        assertEquals("ENFILEIRADO", res.status());
        assertEquals(1, eventosPublicados.size());

        // Processa o mesmo evento duas vezes para validar idempotência
        service.processarEvento(eventosPublicados.get(0));
        service.processarEvento(eventosPublicados.get(0));

        assertEquals(1, bancoMemoria.size());
        assertEquals("Almoço", bancoMemoria.get(0).getDescricao());
    }

    private TransacaoService criarService(List<TransacaoKafkaEvent> eventos, List<Transacao> banco) {
        List<String> processedIds = new ArrayList<>();

        IContaRepository contaRepo = new IContaRepository() {
            @Override public List<Conta> listarPorUsuario(Long usuarioId) { return List.of(); }
            @Override public Optional<Conta> buscarPorIdEUsuario(Long id, Long usuarioId) {
                return Optional.of(new Conta(id, "Conta " + id, BigDecimal.ZERO, usuarioId));
            }
            @Override public Conta salvar(Conta conta) { return conta; }
            @Override public void excluir(Long id, Long usuarioId) {}
        };

        ICategoriaRepository catRepo = new ICategoriaRepository() {
            @Override public List<Categoria> listarPorUsuario(Long usuarioId) { return List.of(); }
            @Override public Optional<Categoria> buscarPorIdEUsuario(Long id, Long usuarioId) {
                return Optional.of(new Categoria(id, "Cat " + id, TipoCategoriaEnum.A, usuarioId));
            }
            @Override public Optional<Categoria> buscarCategoriaTransferencia(Long usuarioId) { return Optional.empty(); }
            @Override public Categoria salvar(Categoria categoria) { return categoria; }
            @Override public void excluir(Long id, Long usuarioId) {}
        };

        ITransacaoRepository transRepo = new ITransacaoRepository() {
            @Override public List<Transacao> listarPorUsuario(Long usuarioId) { return banco; }
            @Override public List<Transacao> listarUltimasPorUsuario(Long usuarioId, int limite) { return banco; }
            @Override public List<Transacao> listarFiltradas(Long usuarioId, TransacaoModels.TransacaoFiltro f, LocalDateTime i, LocalDateTime fim) { return banco; }
            @Override public Optional<Transacao> buscarPorIdEUsuario(Long id, Long usuarioId) { return Optional.empty(); }
            @Override public long contarPorConta(Long contaId, Long usuarioId) { return 0; }
            @Override public long contarPorCategoria(Long categoriaId, Long usuarioId) { return 0; }
            @Override public boolean existePorCorrelationId(String correlationId) { return processedIds.contains(correlationId); }
            @Override public Transacao salvar(Transacao transacao, String correlationId) {
                if (correlationId != null) {
                    processedIds.add(correlationId);
                }
                banco.add(transacao);
                return transacao;
            }
            @Override public void salvarEmLote(List<Transacao> transacoes, String correlationId) {
                if (correlationId != null) {
                    processedIds.add(correlationId);
                }
                banco.addAll(transacoes);
            }
            @Override public void excluir(Long id, Long usuarioId) {}
            @Override public void excluirPorTransferencia(String transferenciaId, Long usuarioId) {}
        };

        ITransacaoEventPublisher publisher = eventos::add;
        return new TransacaoService(transRepo, contaRepo, catRepo, publisher);
    }
}
