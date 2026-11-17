package br.edu.setrem.financeiro.web.exceptions;

import br.edu.setrem.financeiro.implementation.exceptions.AppException;
import br.edu.setrem.financeiro.implementation.models.DashboardModels;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Converte exceções de domínio e aplicação em respostas HTTP JSON padronizadas.
 */
@Provider
public class AppExceptionMapper implements ExceptionMapper<AppException> {

    @Override
    public Response toResponse(AppException exception) {
        return Response.status(exception.getStatusCode())
                .type(MediaType.APPLICATION_JSON)
                .entity(new DashboardModels.ErrorResponse(exception.getMessage()))
                .build();
    }
}
