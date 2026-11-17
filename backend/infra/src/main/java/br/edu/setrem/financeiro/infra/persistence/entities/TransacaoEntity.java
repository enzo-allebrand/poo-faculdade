package br.edu.setrem.financeiro.infra.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidade JPA para a tabela transacao, indexada para alto volume de consultas e idempotência Kafka.
 */
@Entity
@Table(name = "transacao", indexes = {
        @Index(name = "idx_transacao_usuario_data", columnList = "usuario_id, datatransacao"),
        @Index(name = "idx_transacao_conta", columnList = "conta_id"),
        @Index(name = "idx_transacao_categoria", columnList = "categoria_id"),
        @Index(name = "idx_transacao_correlation", columnList = "correlation_id")
})
public class TransacaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false, length = 500)
    public String descricao;

    @Column(nullable = false, precision = 12, scale = 2)
    public BigDecimal valor;

    @Column(nullable = false, length = 1)
    public String tipo;

    @Column(nullable = false)
    public LocalDateTime datatransacao;

    @Column(name = "transferencia_id", length = 64)
    public String transferenciaId;

    @Column(name = "correlation_id", length = 64)
    public String correlationId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "conta_id")
    public ContaEntity conta;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    public CategoriaEntity categoria;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    public UsuarioEntity usuario;
}
