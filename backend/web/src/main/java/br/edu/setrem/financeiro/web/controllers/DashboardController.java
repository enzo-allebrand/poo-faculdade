package br.edu.setrem.financeiro.web.controllers;

import br.edu.setrem.financeiro.implementation.models.DashboardModels;
import br.edu.setrem.financeiro.implementation.services.IDashboardService;
import br.edu.setrem.financeiro.web.security.CurrentUserService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * Controller fino da tela inicial (dashboard).
 */
@Path("Dashboard")
@RolesAllowed("USER")
@Produces(MediaType.APPLICATION_JSON)
public class DashboardController {

    private final IDashboardService dashboardService;
    private final CurrentUserService currentUserService;

    @Inject
    public DashboardController(IDashboardService dashboardService, CurrentUserService currentUserService) {
        this.dashboardService = dashboardService;
        this.currentUserService = currentUserService;
    }

    @GET
    public DashboardModels.DashboardResumoDTO resumo() {
        return dashboardService.montarResumo(currentUserService.requireUserId());
    }
}
