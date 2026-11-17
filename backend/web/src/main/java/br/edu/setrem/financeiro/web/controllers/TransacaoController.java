package br.edu.setrem.financeiro.web.controllers;

import br.edu.setrem.financeiro.implementation.models.TransacaoModels;
import br.edu.setrem.financeiro.implementation.services.ITransacaoService;
import br.edu.setrem.financeiro.web.security.CurrentUserService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

/**
 * Controller fino de transações e transferências assíncronas via Kafka.
 */
@Path("Transacao")
@RolesAllowed("USER")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TransacaoController {

    private final ITransacaoService transacaoService;
    private final CurrentUserService currentUserService;

    @Inject
    public TransacaoController(ITransacaoService transacaoService, CurrentUserService currentUserService) {
        this.transacaoService = transacaoService;
        this.currentUserService = currentUserService;
    }

    @GET
    @Path("findAll")
    public List<TransacaoModels.TransacaoResponseDTO> findAll(
            @QueryParam("contaId") Long contaId,
            @QueryParam("categoriaId") Long categoriaId,
            @QueryParam("tipo") String tipo,
            @QueryParam("dataInicio") String dataInicio,
            @QueryParam("dataFim") String dataFim,
            @QueryParam("busca") String busca
    ) {
        TransacaoModels.TransacaoFiltro filtro = new TransacaoModels.TransacaoFiltro(
                contaId, categoriaId, tipo, dataInicio, dataFim, busca
        );
        return transacaoService.listarFiltradas(currentUserService.requireUserId(), filtro);
    }

    @POST
    public Response create(@Valid TransacaoModels.TransacaoRequest request) {
        TransacaoModels.EventoEnfileiradoDTO enfileirado =
                transacaoService.solicitarLancamento(currentUserService.requireUserId(), request);
        return Response.accepted(enfileirado).build();
    }

    @POST
    @Path("transferencia")
    public Response transferir(@Valid TransacaoModels.TransferenciaDTO request) {
        TransacaoModels.EventoEnfileiradoDTO enfileirado =
                transacaoService.solicitarTransferencia(currentUserService.requireUserId(), request);
        return Response.accepted(enfileirado).build();
    }

    @PUT
    @Path("{id}")
    public Response update(@PathParam("id") Long id, @Valid TransacaoModels.TransacaoRequest request) {
        return Response.ok(transacaoService.atualizar(currentUserService.requireUserId(), id, request)).build();
    }

    @DELETE
    @Path("{id}")
    public Response delete(@PathParam("id") Long id, @QueryParam("excluirPar") @DefaultValue("true") boolean excluirPar) {
        transacaoService.excluir(currentUserService.requireUserId(), id, excluirPar);
        return Response.noContent().build();
    }
}
