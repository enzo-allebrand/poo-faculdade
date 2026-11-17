package br.edu.setrem.financeiro.web.security;

import br.edu.setrem.financeiro.implementation.exceptions.NaoAutorizadoException;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;

/**
 * Extrai o identificador do usuário autenticado a partir do JWT no cookie HttpOnly.
 */
@RequestScoped
public class CurrentUserService {

    @Inject
    JsonWebToken jwt;

    public Long requireUserId() {
        Object rawUserId = jwt.getClaim("userId");
        if (rawUserId == null) {
            throw new NaoAutorizadoException("Usuário não autenticado.");
        }
        try {
            return Long.valueOf(rawUserId.toString());
        } catch (NumberFormatException e) {
            throw new NaoAutorizadoException("Token inválido.");
        }
    }
}
