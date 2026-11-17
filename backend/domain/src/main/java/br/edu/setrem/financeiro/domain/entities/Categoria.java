package br.edu.setrem.financeiro.domain.entities;

import br.edu.setrem.financeiro.domain.enums.TipoCategoriaEnum;

/**
 * Entidade de domínio que representa uma categoria de lançamentos financeiros.
 */
public class Categoria {

    private Long id;
    private String descricao;
    private TipoCategoriaEnum tipo = TipoCategoriaEnum.A;
    private Long usuarioId;

    public Categoria() {
    }

    public Categoria(Long id, String descricao, TipoCategoriaEnum tipo, Long usuarioId) {
        this.id = id;
        this.descricao = descricao;
        this.tipo = tipo != null ? tipo : TipoCategoriaEnum.A;
        this.usuarioId = usuarioId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public TipoCategoriaEnum getTipo() { return tipo; }
    public void setTipo(TipoCategoriaEnum tipo) {
        this.tipo = tipo != null ? tipo : TipoCategoriaEnum.A;
    }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
}
