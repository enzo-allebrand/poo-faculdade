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

/**
 * Entidade JPA para a tabela conta.
 */
@Entity
@Table(name = "conta", indexes = {
        @Index(name = "idx_conta_usuario", columnList = "usuario_id")
})
public class ContaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false, length = 150)
    public String descricao;

    @Column(name = "saldo_inicial", nullable = false, precision = 12, scale = 2)
    public BigDecimal saldoInicial = BigDecimal.ZERO;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    public UsuarioEntity usuario;
}
