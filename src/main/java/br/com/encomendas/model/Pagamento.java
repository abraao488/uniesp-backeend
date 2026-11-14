package br.com.encomendas.model;

import br.com.encomendas.model.enums.FormaPagamento;
import br.com.encomendas.model.enums.StatusPedido;
import br.com.encomendas.model.enums.TipoPagamento;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagamentos")
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoPagamento tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FormaPagamento forma;

    @Column(name = "data_pagamento", nullable = false)
    private LocalDateTime dataPagamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registrado_por_id", nullable = false)
    private Funcionario registradoPor;

    protected Pagamento() {}

    public Pagamento(Pedido pedido, BigDecimal valor, TipoPagamento tipo, FormaPagamento forma,
                     LocalDateTime dataPagamento, Funcionario registradoPor) {
        if (pedido == null) throw new RegraDeNegocioException("Pedido obrigatorio");
        if (pedido.getStatus() == StatusPedido.CANCELADO) {
            throw new RegraDeNegocioException("Registro bloqueado para pedido CANCELADO");
        }
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraDeNegocioException("Valor deve ser maior que zero");
        }
        if (tipo == null) throw new RegraDeNegocioException("Tipo de pagamento obrigatorio");
        if (forma == null) throw new RegraDeNegocioException("Forma de pagamento obrigatoria");
        if (dataPagamento == null) throw new RegraDeNegocioException("Data de pagamento obrigatoria");
        if (registradoPor == null) throw new RegraDeNegocioException("Funcionario registradoPor obrigatorio");

        this.pedido = pedido;
        this.valor = valor;
        this.tipo = tipo;
        this.forma = forma;
        this.dataPagamento = dataPagamento;
        this.registradoPor = registradoPor;

        pedido.adicionarPagamentoInterno(this);
    }

    public Long getId() { return id; }
    public Pedido getPedido() { return pedido; }
    public BigDecimal getValor() { return valor; }
    public TipoPagamento getTipo() { return tipo; }
    public FormaPagamento getForma() { return forma; }
    public LocalDateTime getDataPagamento() { return dataPagamento; }
    public Funcionario getRegistradoPor() { return registradoPor; }
}