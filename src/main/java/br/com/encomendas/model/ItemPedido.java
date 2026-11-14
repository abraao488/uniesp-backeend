package br.com.encomendas.model;

import br.com.encomendas.model.enums.StatusVersao;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "itens_pedido")
public class ItemPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "versao_pedido_id", nullable = false)
    private VersaoPedido versaoPedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @Column(nullable = false)
    private int quantidade;

    @Column(name = "preco_unitario", precision = 10, scale = 2, nullable = false)
    private BigDecimal precoUnitario;

    @Column(columnDefinition = "TEXT")
    private String personalizacao;

    protected ItemPedido() {}

    public ItemPedido(VersaoPedido versaoPedido, Produto produto, int quantidade, BigDecimal precoUnitario, String personalizacao) {
        if (versaoPedido == null) throw new RegraDeNegocioException("Versao do pedido obrigatoria");
        if (versaoPedido.getStatus() != StatusVersao.PENDENTE_APROVACAO) {
            throw new RegraDeNegocioException("So pode adicionar item em versao PENDENTE_APROVACAO");
        }
        if (produto == null) throw new RegraDeNegocioException("Produto obrigatorio");
        if (quantidade <= 0) throw new RegraDeNegocioException("Quantidade deve ser maior que zero");
        if (precoUnitario == null || precoUnitario.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraDeNegocioException("Preco unitario deve ser maior que zero");
        }

        this.versaoPedido = versaoPedido;
        this.produto = produto;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
        this.personalizacao = personalizacao != null ? personalizacao.trim() : null;

        versaoPedido.adicionarItemInterno(this);
    }

    public Long getId() { return id; }
    public VersaoPedido getVersaoPedido() { return versaoPedido; }
    public Produto getProduto() { return produto; }
    public int getQuantidade() { return quantidade; }
    public BigDecimal getPrecoUnitario() { return precoUnitario; }
    public String getPersonalizacao() { return personalizacao; }

    public BigDecimal getSubtotal() {
        return precoUnitario.multiply(BigDecimal.valueOf(quantidade));
    }

    public BigDecimal subtotal() {
        return getSubtotal();
    }

    public void alterarQuantidade(int novaQuantidade) {
        if (versaoPedido != null && versaoPedido.getStatus() != StatusVersao.PENDENTE_APROVACAO) {
            throw new RegraDeNegocioException("So pode alterar item em versao PENDENTE_APROVACAO");
        }
        if (novaQuantidade <= 0) throw new RegraDeNegocioException("Quantidade deve ser maior que zero");
        this.quantidade = novaQuantidade;
    }
}