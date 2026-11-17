package br.edu.setrem.financeiro.infra.security;

import br.edu.setrem.financeiro.domain.entities.Usuario;
import br.edu.setrem.financeiro.implementation.services.ITokenService;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.util.Set;

/**
 * Emite tokens JWT assinados com chave privada RSA (RS256).
 */
@ApplicationScoped
public class JwtTokenService implements ITokenService {

    @ConfigProperty(name = "smallrye.jwt.new-token.issuer", defaultValue = "financeiro-api")
    String issuer;

    @ConfigProperty(name = "app.auth.cookie-max-age-seconds", defaultValue = "86400")
    int maxAgeSeconds;

    @Override
    public String gerarToken(Usuario usuario) {
        return Jwt.issuer(issuer)
                .subject(usuario.getEmail())
                .upn(usuario.getEmail())
                .groups(Set.of("USER"))
                .claim("userId", usuario.getId())
                .claim("nome", usuario.getNome())
                .expiresIn(Duration.ofSeconds(maxAgeSeconds))
                .sign();
    }
}
