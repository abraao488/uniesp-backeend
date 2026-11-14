package br.com.encomendas.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "produtos")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "preco_base", precision = 10, scale = 2, nullable = false)
    private BigDecimal precoBase;

    @Column(nullable = false)
    private boolean ativo = true;

    @OneToMany(mappedBy = "produto", fetch = FetchType.LAZY)
    private java.util.List<ItemPedido> itens = new java.util.ArrayList<>();

    protected Produto() {}

    public Produto(String nome, String descricao, BigDecimal precoBase, boolean ativo) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new RegraDeNegocioException("Nome obrigatorio");
        }
        if (precoBase == null || precoBase.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraDeNegocioException("Preco base deve ser maior que zero");
        }

        this.nome = nome.trim();
        this.descricao = descricao != null ? descricao.trim() : null;
        this.precoBase = precoBase;
        this.ativo = ativo;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
    public BigDecimal getPrecoBase() { return precoBase; }
    public boolean isAtivo() { return ativo; }
    public void ativar() { this.ativo = true; }
    public void desativar() { this.ativo = false; }
    public void reativar() { this.ativo = true; }
    public void alterarPreco(BigDecimal novoPreco) {
        if (novoPreco == null || novoPreco.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraDeNegocioException("Preco deve ser maior que zero");
        }
        this.precoBase = novoPreco;
    }

    public java.util.List<ItemPedido> getItens() { return java.util.Collections.unmodifiableList(itens); }

    public void alterarDescricao(String novaDescricao) {
        if (novaDescricao != null) {
            this.descricao = novaDescricao.trim();
        }
    }
}