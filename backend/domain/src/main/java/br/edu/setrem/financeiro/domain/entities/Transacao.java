package br.edu.setrem.financeiro.domain.entities;

import br.edu.setrem.financeiro.domain.enums.TipoTransacaoEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidade de domínio que representa uma transação financeira (receita, despesa ou transferência).
 */
public class Transacao {

    private Long id;
    private String descricao;
    private BigDecimal valor;
    private TipoTransacaoEnum tipo;
    private LocalDateTime datatransacao;
    private String transferenciaId;
    private Long contaId;
    private String contaDescricao;
    private Long categoriaId;
    private String categoriaDescricao;
    private Long usuarioId;

    public Transacao() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }

    public TipoTransacaoEnum getTipo() { return tipo; }
    public void setTipo(TipoTransacaoEnum tipo) { this.tipo = tipo; }

    public LocalDateTime getDatatransacao() { return datatransacao; }
    public void setDatatransacao(LocalDateTime datatransacao) { this.datatransacao = datatransacao; }

    public String getTransferenciaId() { return transferenciaId; }
    public void setTransferenciaId(String transferenciaId) { this.transferenciaId = transferenciaId; }

    public Long getContaId() { return contaId; }
    public void setContaId(Long contaId) { this.contaId = contaId; }

    public String getContaDescricao() { return contaDescricao; }
    public void setContaDescricao(String contaDescricao) { this.contaDescricao = contaDescricao; }

    public Long getCategoriaId() { return categoriaId; }
    public void setCategoriaId(Long categoriaId) { this.categoriaId = categoriaId; }

    public String getCategoriaDescricao() { return categoriaDescricao; }
    public void setCategoriaDescricao(String categoriaDescricao) { this.categoriaDescricao = categoriaDescricao; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
}
