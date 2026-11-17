package br.edu.setrem.financeiro.domain.enums;

/**
 * Aplicabilidade da categoria: Receita (R), Despesa (D) ou Ambos (A).
 */
public enum TipoCategoriaEnum {
    R("Receita"),
    D("Despesa"),
    A("Ambos");

    private final String descricao;

    TipoCategoriaEnum(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public static TipoCategoriaEnum fromCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return A;
        }
        for (TipoCategoriaEnum tipo : values()) {
            if (tipo.name().equalsIgnoreCase(codigo.trim())) {
                return tipo;
            }
        }
        return null;
    }
}
