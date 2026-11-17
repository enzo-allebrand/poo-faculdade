package br.edu.setrem.financeiro.web.controllers;

import br.edu.setrem.financeiro.implementation.models.CategoriaModels;
import br.edu.setrem.financeiro.implementation.services.ICategoriaService;
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
 * Controller fino de categorias financeiras.
 */
@Path("Categoria")
@RolesAllowed("USER")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CategoriaController {

    private final ICategoriaService categoriaService;
    private final CurrentUserService currentUserService;

    @Inject
    public CategoriaController(ICategoriaService categoriaService, CurrentUserService currentUserService) {
        this.categoriaService = categoriaService;
        this.currentUserService = currentUserService;
    }

    @GET
    @Path("findAll")
    public List<CategoriaModels.CategoriaDTO> findAll() {
        return categoriaService.listar(currentUserService.requireUserId());
    }

    @POST
    public Response create(@Valid CategoriaModels.CategoriaRequest request) {
        CategoriaModels.CategoriaDTO criada = categoriaService.criar(currentUserService.requireUserId(), request);
        return Response.status(Response.Status.CREATED).entity(criada).build();
    }

    @PUT
    @Path("{id}")
    public Response update(@PathParam("id") Long id, @Valid CategoriaModels.CategoriaRequest request) {
        return Response.ok(categoriaService.atualizar(currentUserService.requireUserId(), id, request)).build();
    }

    @DELETE
    @Path("{id}")
    public Response delete(@PathParam("id") Long id) {
        categoriaService.excluir(currentUserService.requireUserId(), id);
        return Response.noContent().build();
    }
}
