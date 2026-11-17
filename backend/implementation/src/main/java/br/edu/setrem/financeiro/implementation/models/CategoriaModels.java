package br.edu.setrem.financeiro.implementation.models;

import br.edu.setrem.financeiro.domain.entities.Categoria;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Modelos e contratos de entrada/saída para categorias financeiras.
 */
public final class CategoriaModels {

    private CategoriaModels() {
    }

    public record CategoriaRequest(
            @NotBlank(message = "Descrição da categoria é obrigatória")
            @Size(max = 150, message = "Descrição deve ter no máximo 150 caracteres")
            String descricao,

            @Pattern(regexp = "^[RDA]$", message = "Tipo da categoria deve ser 'R', 'D' ou 'A'")
            String tipo
    ) {
    }

    public record CategoriaDTO(Long id, String descricao, String tipo) {
        public static CategoriaDTO from(Categoria categoria) {
            return new CategoriaDTO(categoria.getId(), categoria.getDescricao(), categoria.getTipo().name());
        }
    }
}
