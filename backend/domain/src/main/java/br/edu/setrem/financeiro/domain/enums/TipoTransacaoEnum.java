package br.edu.setrem.financeiro.domain.enums;

/**
 * Tipo da movimentação financeira: Receita (R) ou Despesa (D).
 */
public enum TipoTransacaoEnum {
    R("Receita"),
    D("Despesa");

    private final String descricao;

    TipoTransacaoEnum(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public static TipoTransacaoEnum fromCodigo(String codigo) {
        if (codigo == null) {
            return null;
        }
        for (TipoTransacaoEnum tipo : values()) {
            if (tipo.name().equalsIgnoreCase(codigo.trim())) {
                return tipo;
            }
        }
        return null;
    }
}
