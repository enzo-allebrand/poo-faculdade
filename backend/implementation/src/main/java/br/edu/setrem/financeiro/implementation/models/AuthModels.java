package br.edu.setrem.financeiro.implementation.models;

import br.edu.setrem.financeiro.domain.entities.Usuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Modelos e contratos de entrada/saída para autenticação.
 */
public final class AuthModels {

    private AuthModels() {
    }

    public record RegisterRequest(
            @NotBlank(message = "Nome é obrigatório")
            @Size(max = 120, message = "Nome deve ter no máximo 120 caracteres")
            String nome,

            @NotBlank(message = "E-mail é obrigatório")
            @Email(message = "E-mail inválido")
            @Size(max = 180, message = "E-mail deve ter no máximo 180 caracteres")
            String email,

            @NotBlank(message = "Senha é obrigatória")
            @Size(min = 6, max = 100, message = "Senha deve ter entre 6 e 100 caracteres")
            String senha
    ) {
    }

    public record LoginRequest(
            @NotBlank(message = "E-mail é obrigatório")
            @Email(message = "E-mail inválido")
            String email,

            @NotBlank(message = "Senha é obrigatória")
            String senha
    ) {
    }

    public record UserResponse(Long id, String nome, String email) {
        public static UserResponse from(Usuario usuario) {
            return new UserResponse(usuario.getId(), usuario.getNome(), usuario.getEmail());
        }
    }

    public record AuthResult(UserResponse user, String token) {
    }
}
