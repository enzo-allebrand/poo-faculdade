package br.edu.setrem.financeiro.application.services;

import br.edu.setrem.financeiro.domain.entities.Conta;
import br.edu.setrem.financeiro.domain.entities.Transacao;
import br.edu.setrem.financeiro.domain.enums.TipoTransacaoEnum;
import br.edu.setrem.financeiro.implementation.exceptions.ConflitoException;
import br.edu.setrem.financeiro.implementation.exceptions.RecursoNaoEncontradoException;
import br.edu.setrem.financeiro.implementation.models.ContaModels;
import br.edu.setrem.financeiro.implementation.repositories.IContaRepository;
import br.edu.setrem.financeiro.implementation.repositories.ITransacaoRepository;
import br.edu.setrem.financeiro.implementation.services.IContaService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Regras de negócio para contas financeiras e apuração de saldo por conta.
 */
@ApplicationScoped
public class ContaService implements IContaService {

    private final IContaRepository contaRepository;
    private final ITransacaoRepository transacaoRepository;

    @Inject
    public ContaService(IContaRepository contaRepository, ITransacaoRepository transacaoRepository) {
        this.contaRepository = contaRepository;
        this.transacaoRepository = transacaoRepository;
    }

    @Override
    public List<ContaModels.ContaSaldoDTO> listarComSaldos(Long usuarioId) {
        List<Conta> contas = contaRepository.listarPorUsuario(usuarioId);
        List<Transacao> transacoes = transacaoRepository.listarPorUsuario(usuarioId);

        Map<Long, BigDecimal> receitasPorConta = new HashMap<>();
        Map<Long, BigDecimal> despesasPorConta = new HashMap<>();

        for (Transacao t : transacoes) {
            if (t.getTipo() == TipoTransacaoEnum.R) {
                receitasPorConta.merge(t.getContaId(), t.getValor(), BigDecimal::add);
            } else if (t.getTipo() == TipoTransacaoEnum.D) {
                despesasPorConta.merge(t.getContaId(), t.getValor(), BigDecimal::add);
            }
        }

        return contas.stream().map(conta -> {
            BigDecimal inicial = conta.getSaldoInicial() != null ? conta.getSaldoInicial() : BigDecimal.ZERO;
            BigDecimal receitas = receitasPorConta.getOrDefault(conta.getId(), BigDecimal.ZERO);
            BigDecimal despesas = despesasPorConta.getOrDefault(conta.getId(), BigDecimal.ZERO);
            BigDecimal saldoAtual = inicial.add(receitas).subtract(despesas);
            return new ContaModels.ContaSaldoDTO(conta.getId(), conta.getDescricao(), inicial, receitas, despesas, saldoAtual);
        }).toList();
    }

    @Override
    @Transactional
    public ContaModels.ContaSaldoDTO criar(Long usuarioId, ContaModels.ContaRequest request) {
        Conta conta = new Conta();
        conta.setDescricao(request.descricao().trim());
        conta.setSaldoInicial(request.saldoInicial() != null ? request.saldoInicial() : BigDecimal.ZERO);
        conta.setUsuarioId(usuarioId);

        Conta salva = contaRepository.salvar(conta);
        return new ContaModels.ContaSaldoDTO(
                salva.getId(),
                salva.getDescricao(),
                salva.getSaldoInicial(),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                salva.getSaldoInicial()
        );
    }

    @Override
    @Transactional
    public ContaModels.ContaSaldoDTO atualizar(Long usuarioId, Long contaId, ContaModels.ContaRequest request) {
        Conta conta = contaRepository.buscarPorIdEUsuario(contaId, usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conta não encontrada."));

        conta.setDescricao(request.descricao().trim());
        if (request.saldoInicial() != null) {
            conta.setSaldoInicial(request.saldoInicial());
        }
        contaRepository.salvar(conta);

        return listarComSaldos(usuarioId).stream()
                .filter(c -> c.id().equals(contaId))
                .findFirst()
                .orElseThrow();
    }

    @Override
    @Transactional
    public void excluir(Long usuarioId, Long contaId) {
        contaRepository.buscarPorIdEUsuario(contaId, usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conta não encontrada."));

        if (transacaoRepository.contarPorConta(contaId, usuarioId) > 0) {
            throw new ConflitoException("Não é possível excluir uma conta que possui transações vinculadas.");
        }

        contaRepository.excluir(contaId, usuarioId);
    }
}
