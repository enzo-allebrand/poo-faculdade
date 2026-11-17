package br.edu.setrem.financeiro.domain.entities;

import java.math.BigDecimal;

/**
 * Entidade de domínio que representa uma conta financeira do usuário.
 */
public class Conta {

    private Long id;
    private String descricao;
    private BigDecimal saldoInicial = BigDecimal.ZERO;
    private Long usuarioId;

    public Conta() {
    }

    public Conta(Long id, String descricao, BigDecimal saldoInicial, Long usuarioId) {
        this.id = id;
        this.descricao = descricao;
        this.saldoInicial = saldoInicial != null ? saldoInicial : BigDecimal.ZERO;
        this.usuarioId = usuarioId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public BigDecimal getSaldoInicial() { return saldoInicial; }
    public void setSaldoInicial(BigDecimal saldoInicial) {
        this.saldoInicial = saldoInicial != null ? saldoInicial : BigDecimal.ZERO;
    }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
}
