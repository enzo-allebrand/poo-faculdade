package br.edu.setrem.financeiro.application.services;

import br.edu.setrem.financeiro.implementation.models.ContaModels;
import br.edu.setrem.financeiro.implementation.models.DashboardModels;
import br.edu.setrem.financeiro.implementation.models.TransacaoModels;
import br.edu.setrem.financeiro.implementation.repositories.ITransacaoRepository;
import br.edu.setrem.financeiro.implementation.services.IContaService;
import br.edu.setrem.financeiro.implementation.services.IDashboardService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.util.List;

/**
 * Monta o resumo financeiro da tela inicial com saldo geral, por conta e últimas movimentações.
 */
@ApplicationScoped
public class DashboardService implements IDashboardService {

    private final IContaService contaService;
    private final ITransacaoRepository transacaoRepository;

    @Inject
    public DashboardService(IContaService contaService, ITransacaoRepository transacaoRepository) {
        this.contaService = contaService;
        this.transacaoRepository = transacaoRepository;
    }

    @Override
    public DashboardModels.DashboardResumoDTO montarResumo(Long usuarioId) {
        List<ContaModels.ContaSaldoDTO> contas = contaService.listarComSaldos(usuarioId);

        BigDecimal saldoGeral = BigDecimal.ZERO;
        BigDecimal totalReceitas = BigDecimal.ZERO;
        BigDecimal totalDespesas = BigDecimal.ZERO;

        for (ContaModels.ContaSaldoDTO conta : contas) {
            saldoGeral = saldoGeral.add(conta.saldoAtual());
            totalReceitas = totalReceitas.add(conta.totalReceitas());
            totalDespesas = totalDespesas.add(conta.totalDespesas());
        }

        List<TransacaoModels.TransacaoResponseDTO> ultimas = transacaoRepository
                .listarUltimasPorUsuario(usuarioId, 8)
                .stream()
                .map(TransacaoModels.TransacaoResponseDTO::from)
                .toList();

        return new DashboardModels.DashboardResumoDTO(saldoGeral, totalReceitas, totalDespesas, contas, ultimas);
    }
}
