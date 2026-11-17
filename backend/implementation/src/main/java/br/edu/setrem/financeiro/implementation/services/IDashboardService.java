package br.edu.setrem.financeiro.implementation.services;

import br.edu.setrem.financeiro.implementation.models.DashboardModels;

/**
 * Contrato do resumo financeiro da tela inicial.
 */
public interface IDashboardService {
    DashboardModels.DashboardResumoDTO montarResumo(Long usuarioId);
}
