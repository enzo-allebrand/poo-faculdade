package br.edu.setrem.financeiro.infra.security;

import br.edu.setrem.financeiro.implementation.services.IPasswordHasher;
import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Implementação BCrypt para hash seguro de senhas.
 */
@ApplicationScoped
public class BcryptPasswordHasher implements IPasswordHasher {

    @Override
    public String hash(String senhaPura) {
        return BcryptUtil.bcryptHash(senhaPura);
    }

    @Override
    public boolean matches(String senhaPura, String senhaHash) {
        return BcryptUtil.matches(senhaPura, senhaHash);
    }
}
