package br.edu.setrem.financeiro.web.controllers;

import br.edu.setrem.financeiro.implementation.models.AuthModels;
import br.edu.setrem.financeiro.implementation.services.IAuthService;
import br.edu.setrem.financeiro.web.security.CurrentUserService;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Controller fino de autenticação via JWT em cookie HttpOnly.
 */
@Path("auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthController {

    private final IAuthService authService;
    private final CurrentUserService currentUserService;

    @ConfigProperty(name = "app.auth.cookie-name", defaultValue = "access_token")
    String cookieName;

    @ConfigProperty(name = "app.auth.cookie-max-age-seconds", defaultValue = "86400")
    int cookieMaxAge;

    @ConfigProperty(name = "app.auth.cookie-secure", defaultValue = "false")
    boolean cookieSecure;

    @Inject
    public AuthController(IAuthService authService, CurrentUserService currentUserService) {
        this.authService = authService;
        this.currentUserService = currentUserService;
    }

    @POST
    @Path("register")
    @PermitAll
    public Response register(@Valid AuthModels.RegisterRequest request) {
        AuthModels.AuthResult result = authService.registrar(request);
        return Response.status(Response.Status.CREATED)
                .entity(result.user())
                .cookie(buildAuthCookie(result.token(), cookieMaxAge))
                .build();
    }

    @POST
    @Path("login")
    @PermitAll
    public Response login(@Valid AuthModels.LoginRequest request) {
        AuthModels.AuthResult result = authService.autenticar(request);
        return Response.ok(result.user())
                .cookie(buildAuthCookie(result.token(), cookieMaxAge))
                .build();
    }

    @POST
    @Path("logout")
    @PermitAll
    @Consumes(MediaType.WILDCARD)
    public Response logout() {
        return Response.noContent()
                .cookie(buildAuthCookie("", 0))
                .build();
    }

    @GET
    @Path("me")
    @RolesAllowed("USER")
    public AuthModels.UserResponse me() {
        return authService.obterUsuarioAtual(currentUserService.requireUserId());
    }

    private NewCookie buildAuthCookie(String value, int maxAge) {
        return new NewCookie.Builder(cookieName)
                .value(value)
                .path("/")
                .maxAge(maxAge)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(NewCookie.SameSite.LAX)
                .build();
    }
}
