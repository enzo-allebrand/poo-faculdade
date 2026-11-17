package br.edu.setrem.financeiro.application.services;

import br.edu.setrem.financeiro.domain.entities.Categoria;
import br.edu.setrem.financeiro.domain.entities.Conta;
import br.edu.setrem.financeiro.domain.entities.Transacao;
import br.edu.setrem.financeiro.domain.enums.TipoCategoriaEnum;
import br.edu.setrem.financeiro.domain.enums.TipoTransacaoEnum;
import br.edu.setrem.financeiro.implementation.exceptions.RecursoNaoEncontradoException;
import br.edu.setrem.financeiro.implementation.exceptions.RegraNegocioException;
import br.edu.setrem.financeiro.implementation.models.TransacaoKafkaEvent;
import br.edu.setrem.financeiro.implementation.models.TransacaoModels;
import br.edu.setrem.financeiro.implementation.repositories.ICategoriaRepository;
import br.edu.setrem.financeiro.implementation.repositories.IContaRepository;
import br.edu.setrem.financeiro.implementation.repositories.ITransacaoRepository;
import br.edu.setrem.financeiro.implementation.services.ITransacaoEventPublisher;
import br.edu.setrem.financeiro.implementation.services.ITransacaoService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;

/**
 * Regras de negócio para transações financeiras, filtros e ingestão assíncrona via Kafka.
 */
@ApplicationScoped
public class TransacaoService implements ITransacaoService {

    private final ITransacaoRepository transacaoRepository;
    private final IContaRepository contaRepository;
    private final ICategoriaRepository categoriaRepository;
    private final ITransacaoEventPublisher eventPublisher;

    @Inject
    public TransacaoService(ITransacaoRepository transacaoRepository, IContaRepository contaRepository, ICategoriaRepository categoriaRepository, ITransacaoEventPublisher eventPublisher) {
        this.transacaoRepository = transacaoRepository;
        this.contaRepository = contaRepository;
        this.categoriaRepository = categoriaRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public List<TransacaoModels.TransacaoResponseDTO> listarFiltradas(Long usuarioId, TransacaoModels.TransacaoFiltro filtro) {
        LocalDateTime inicio = null;
        LocalDateTime fim = null;

        try {
            if (filtro.dataInicio() != null && !filtro.dataInicio().isBlank()) {
                inicio = parseDateBoundary(filtro.dataInicio().trim(), true);
            }
            if (filtro.dataFim() != null && !filtro.dataFim().isBlank()) {
                fim = parseDateBoundary(filtro.dataFim().trim(), false);
            }
        } catch (DateTimeParseException e) {
            throw new RegraNegocioException("Formato de data inválido. Use yyyy-MM-dd.");
        }

        return transacaoRepository.listarFiltradas(usuarioId, filtro, inicio, fim)
                .stream()
                .map(TransacaoModels.TransacaoResponseDTO::from)
                .toList();
    }

    @Override
    public TransacaoModels.EventoEnfileiradoDTO solicitarLancamento(Long usuarioId, TransacaoModels.TransacaoRequest request) {
        TipoTransacaoEnum tipo = TipoTransacaoEnum.fromCodigo(request.tipo());
        if (tipo == null) {
            throw new RegraNegocioException("Tipo de transação inválido. Use 'R' ou 'D'.");
        }

        contaRepository.buscarPorIdEUsuario(request.contaId(), usuarioId)
                .orElseThrow(() -> new RegraNegocioException("Conta não encontrada para o usuário."));
        categoriaRepository.buscarPorIdEUsuario(request.categoriaId(), usuarioId)
                .orElseThrow(() -> new RegraNegocioException("Categoria não encontrada para o usuário."));

        String correlationId = UUID.randomUUID().toString();
        LocalDateTime data = request.datatransacao() != null ? request.datatransacao() : LocalDateTime.now();

        TransacaoKafkaEvent evento = new TransacaoKafkaEvent(
                correlationId,
                TransacaoKafkaEvent.OPERACAO_LANCAMENTO,
                usuarioId,
                request.descricao().trim(),
                request.valor(),
                tipo.name(),
                data,
                request.contaId(),
                request.categoriaId(),
                null,
                null,
                null
        );

        eventPublisher.publicar(evento);
        return new TransacaoModels.EventoEnfileiradoDTO(
                "ENFILEIRADO",
                correlationId,
                "Transação enfileirada no Kafka para gravação assíncrona."
        );
    }

    @Override
    public TransacaoModels.EventoEnfileiradoDTO solicitarTransferencia(Long usuarioId, TransacaoModels.TransferenciaDTO request) {
        if (request.contaOrigemId().equals(request.contaDestinoId())) {
            throw new RegraNegocioException("Conta de origem e conta de destino devem ser diferentes.");
        }

        contaRepository.buscarPorIdEUsuario(request.contaOrigemId(), usuarioId)
                .orElseThrow(() -> new RegraNegocioException("Conta de origem não encontrada."));
        contaRepository.buscarPorIdEUsuario(request.contaDestinoId(), usuarioId)
                .orElseThrow(() -> new RegraNegocioException("Conta de destino não encontrada."));

        if (request.categoriaId() != null) {
            categoriaRepository.buscarPorIdEUsuario(request.categoriaId(), usuarioId)
                    .orElseThrow(() -> new RegraNegocioException("Categoria informada não encontrada."));
        }

        String correlationId = UUID.randomUUID().toString();
        String transferenciaId = UUID.randomUUID().toString();
        LocalDateTime data = request.datatransacao() != null ? request.datatransacao() : LocalDateTime.now();
        String descricao = (request.descricao() != null && !request.descricao().isBlank())
                ? request.descricao().trim()
                : "Transferência entre contas";

        TransacaoKafkaEvent evento = new TransacaoKafkaEvent(
                correlationId,
                TransacaoKafkaEvent.OPERACAO_TRANSFERENCIA,
                usuarioId,
                descricao,
                request.valor(),
                null,
                data,
                null,
                request.categoriaId(),
                request.contaOrigemId(),
                request.contaDestinoId(),
                transferenciaId
        );

        eventPublisher.publicar(evento);
        return new TransacaoModels.EventoEnfileiradoDTO(
                "ENFILEIRADO",
                correlationId,
                "Transferência enfileirada no Kafka para gravação assíncrona."
        );
    }

    @Override
    @Transactional
    public TransacaoModels.TransacaoResponseDTO atualizar(Long usuarioId, Long transacaoId, TransacaoModels.TransacaoRequest request) {
        Transacao entity = transacaoRepository.buscarPorIdEUsuario(transacaoId, usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Transação não encontrada."));

        Conta conta = contaRepository.buscarPorIdEUsuario(request.contaId(), usuarioId)
                .orElseThrow(() -> new RegraNegocioException("Conta inválida."));
        Categoria categoria = categoriaRepository.buscarPorIdEUsuario(request.categoriaId(), usuarioId)
                .orElseThrow(() -> new RegraNegocioException("Categoria inválida."));
        TipoTransacaoEnum tipo = TipoTransacaoEnum.fromCodigo(request.tipo());
        if (tipo == null) {
            throw new RegraNegocioException("Tipo inválido.");
        }

        entity.setDescricao(request.descricao().trim());
        entity.setValor(request.valor());
        entity.setTipo(tipo);
        if (request.datatransacao() != null) {
            entity.setDatatransacao(request.datatransacao());
        }
        entity.setContaId(conta.getId());
        entity.setCategoriaId(categoria.getId());

        Transacao atualizada = transacaoRepository.salvar(entity, null);
        return TransacaoModels.TransacaoResponseDTO.from(atualizada);
    }

    @Override
    @Transactional
    public void excluir(Long usuarioId, Long transacaoId, boolean excluirPar) {
        Transacao entity = transacaoRepository.buscarPorIdEUsuario(transacaoId, usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Transação não encontrada."));

        if (excluirPar && entity.getTransferenciaId() != null) {
            transacaoRepository.excluirPorTransferencia(entity.getTransferenciaId(), usuarioId);
        } else {
            transacaoRepository.excluir(transacaoId, usuarioId);
        }
    }

    @Override
    @Transactional
    public void processarEvento(TransacaoKafkaEvent evento) {
        if (evento == null || evento.correlationId() == null) {
            return;
        }
        if (transacaoRepository.existePorCorrelationId(evento.correlationId())) {
            return;
        }

        if (TransacaoKafkaEvent.OPERACAO_LANCAMENTO.equals(evento.operacao())) {
            Transacao transacao = new Transacao();
            transacao.setDescricao(evento.descricao());
            transacao.setValor(evento.valor());
            transacao.setTipo(TipoTransacaoEnum.fromCodigo(evento.tipo()));
            transacao.setDatatransacao(evento.datatransacao());
            transacao.setContaId(evento.contaId());
            transacao.setCategoriaId(evento.categoriaId());
            transacao.setUsuarioId(evento.usuarioId());
            transacaoRepository.salvar(transacao, evento.correlationId());
            return;
        }

        if (TransacaoKafkaEvent.OPERACAO_TRANSFERENCIA.equals(evento.operacao())) {
            Conta origem = contaRepository.buscarPorIdEUsuario(evento.contaOrigemId(), evento.usuarioId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Conta de origem não encontrada no processamento."));
            Conta destino = contaRepository.buscarPorIdEUsuario(evento.contaDestinoId(), evento.usuarioId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Conta de destino não encontrada no processamento."));
            Categoria categoria = resolverCategoriaTransferencia(evento.categoriaId(), evento.usuarioId());

            Transacao saida = new Transacao();
            saida.setDescricao(evento.descricao() + " (para " + destino.getDescricao() + ")");
            saida.setValor(evento.valor());
            saida.setTipo(TipoTransacaoEnum.D);
            saida.setDatatransacao(evento.datatransacao());
            saida.setTransferenciaId(evento.transferenciaId());
            saida.setContaId(origem.getId());
            saida.setCategoriaId(categoria.getId());
            saida.setUsuarioId(evento.usuarioId());

            Transacao entrada = new Transacao();
            entrada.setDescricao(evento.descricao() + " (de " + origem.getDescricao() + ")");
            entrada.setValor(evento.valor());
            entrada.setTipo(TipoTransacaoEnum.R);
            entrada.setDatatransacao(evento.datatransacao());
            entrada.setTransferenciaId(evento.transferenciaId());
            entrada.setContaId(destino.getId());
            entrada.setCategoriaId(categoria.getId());
            entrada.setUsuarioId(evento.usuarioId());

            transacaoRepository.salvarEmLote(List.of(saida, entrada), evento.correlationId());
        }
    }

    private Categoria resolverCategoriaTransferencia(Long categoriaId, Long usuarioId) {
        if (categoriaId != null) {
            return categoriaRepository.buscarPorIdEUsuario(categoriaId, usuarioId)
                    .orElseThrow(() -> new RegraNegocioException("Categoria informada não encontrada."));
        }
        return categoriaRepository.buscarCategoriaTransferencia(usuarioId).orElseGet(() -> {
            Categoria nova = new Categoria();
            nova.setDescricao("Transferência");
            nova.setTipo(TipoCategoriaEnum.A);
            nova.setUsuarioId(usuarioId);
            return categoriaRepository.salvar(nova);
        });
    }

    private LocalDateTime parseDateBoundary(String raw, boolean startOfDay) {
        if (raw.length() == 10) {
            LocalDate date = LocalDate.parse(raw);
            return startOfDay ? date.atStartOfDay() : date.atTime(LocalTime.MAX);
        }
        return LocalDateTime.parse(raw);
    }
}
