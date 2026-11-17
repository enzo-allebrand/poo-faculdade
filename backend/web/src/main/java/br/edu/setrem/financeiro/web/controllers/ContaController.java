package br.edu.setrem.financeiro.web.controllers;

import br.edu.setrem.financeiro.implementation.models.ContaModels;
import br.edu.setrem.financeiro.implementation.models.DashboardModels;
import br.edu.setrem.financeiro.implementation.services.IContaService;
import br.edu.setrem.financeiro.implementation.services.IDashboardService;
import br.edu.setrem.financeiro.web.security.CurrentUserService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

/**
 * Controller fino de contas financeiras.
 */
@Path("Conta")
@RolesAllowed("USER")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ContaController {

    private final IContaService contaService;
    private final IDashboardService dashboardService;
    private final CurrentUserService currentUserService;

    @Inject
    public ContaController(IContaService contaService, IDashboardService dashboardService, CurrentUserService currentUserService) {
        this.contaService = contaService;
        this.dashboardService = dashboardService;
        this.currentUserService = currentUserService;
    }

    @GET
    @Path("findAll")
    public List<ContaModels.ContaSaldoDTO> findAll() {
        return contaService.listarComSaldos(currentUserService.requireUserId());
    }

    @GET
    @Path("saldos")
    public DashboardModels.DashboardResumoDTO saldos() {
        return dashboardService.montarResumo(currentUserService.requireUserId());
    }

    @POST
    public Response create(@Valid ContaModels.ContaRequest request) {
        ContaModels.ContaSaldoDTO criada = contaService.criar(currentUserService.requireUserId(), request);
        return Response.status(Response.Status.CREATED).entity(criada).build();
    }

    @PUT
    @Path("{id}")
    public Response update(@PathParam("id") Long id, @Valid ContaModels.ContaRequest request) {
        return Response.ok(contaService.atualizar(currentUserService.requireUserId(), id, request)).build();
    }

    @DELETE
    @Path("{id}")
    public Response delete(@PathParam("id") Long id) {
        contaService.excluir(currentUserService.requireUserId(), id);
        return Response.noContent().build();
    }
}
