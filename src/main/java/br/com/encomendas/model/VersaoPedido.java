package br.com.encomendas.model;

import br.com.encomendas.model.enums.OrigemAprovacao;
import br.com.encomendas.model.enums.StatusVersao;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "versoes_pedido")
public class VersaoPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @Column(nullable = false)
    private int numero;

    @Column(name = "descricao_personalizacao", columnDefinition = "TEXT")
    private String descricaoPersonalizacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusVersao status = StatusVersao.PENDENTE_APROVACAO;

    @Column(name = "criada_em", nullable = false, updatable = false)
    private LocalDateTime criadaEm;

    @Column(name = "aprovada_em")
    private LocalDateTime aprovadaEm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criada_por_id", nullable = false)
    private Funcionario criadaPor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aprovador_id")
    private Funcionario aprovador;

    @Enumerated(EnumType.STRING)
    @Column(name = "origem_aprovacao", length = 20)
    private OrigemAprovacao origemAprovacao;

    @OneToMany(mappedBy = "versaoPedido", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ItemPedido> itens = new ArrayList<>();

    protected VersaoPedido() {}

    public VersaoPedido(Pedido pedido, int numero, String descricaoPersonalizacao, Funcionario criadaPor) {
        this(pedido, numero, descricaoPersonalizacao, criadaPor, LocalDateTime.now());
    }

    public VersaoPedido(Pedido pedido, String descricaoPersonalizacao, Funcionario criadaPor) {
        this(pedido, nextNumero(pedido), descricaoPersonalizacao, criadaPor, LocalDateTime.now());
    }

    public VersaoPedido(Pedido pedido, int numero, String descricaoPersonalizacao, Funcionario criadaPor, LocalDateTime criadaEm) {
        if (pedido == null) throw new RegraDeNegocioException("Pedido obrigatorio");
        if (numero <= 0) throw new RegraDeNegocioException("Numero da versao deve ser positivo");
        if (criadaPor == null) throw new RegraDeNegocioException("Funcionario criadaPor obrigatorio");
        if (!criadaPor.isAtivo()) throw new RegraDeNegocioException("Funcionario criadaPor deve estar ativo");
        if (criadaEm == null) throw new RegraDeNegocioException("Data de criacao obrigatoria");

        this.pedido = pedido;
        this.numero = numero;
        this.descricaoPersonalizacao = descricaoPersonalizacao != null ? descricaoPersonalizacao.trim() : null;
        this.criadaEm = criadaEm;
        this.criadaPor = criadaPor;
        this.status = StatusVersao.PENDENTE_APROVACAO;

        pedido.adicionarVersaoInterno(this);
    }

    public Long getId() { return id; }
    public Pedido getPedido() { return pedido; }
    public int getNumero() { return numero; }
    public String getDescricaoPersonalizacao() { return descricaoPersonalizacao; }
    public StatusVersao getStatus() { return status; }
    public LocalDateTime getCriadaEm() { return criadaEm; }
    public LocalDateTime getAprovadaEm() { return aprovadaEm; }
    public Funcionario getCriadaPor() { return criadaPor; }
    public Funcionario getAprovador() { return aprovador; }
    public OrigemAprovacao getOrigemAprovacao() { return origemAprovacao; }
    public List<ItemPedido> getItens() { return Collections.unmodifiableList(itens); }

    public BigDecimal getValorTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (ItemPedido item : itens) {
            total = total.add(item.getSubtotal());
        }
        return total;
    }

    public boolean isAprovada() {
        return status == StatusVersao.APROVADA;
    }

    public void aprovar(Funcionario aprovador, OrigemAprovacao origem) {
        if (status != StatusVersao.PENDENTE_APROVACAO) {
            throw new RegraDeNegocioException("So pode aprovar versao PENDENTE_APROVACAO");
        }
        if (origem == null) throw new RegraDeNegocioException("Origem da aprovacao obrigatoria");
        if (aprovador == null && origem != OrigemAprovacao.CLIENTE_VIA_LINK) {
            throw new RegraDeNegocioException("Aprovador obrigatorio");
        }

        this.status = StatusVersao.APROVADA;
        this.aprovadaEm = LocalDateTime.now();
        this.aprovador = aprovador;
        this.origemAprovacao = origem;
    }

    public void aprovar() {
        aprovar(null, OrigemAprovacao.CLIENTE_VIA_LINK);
    }

    public void invalidar() {
        if (status == StatusVersao.APROVADA || status == StatusVersao.PENDENTE_APROVACAO) {
            this.status = StatusVersao.INVALIDADA;
        }
    }

    public ItemPedido adicionarItem(Produto produto, int quantidade, BigDecimal precoUnitario, String personalizacao) {
        if (this.status != StatusVersao.PENDENTE_APROVACAO) {
            throw new RegraDeNegocioException("So pode adicionar item em versao PENDENTE_APROVACAO");
        }
        ItemPedido novoItem = new ItemPedido(this, produto, quantidade, precoUnitario, personalizacao);
        return novoItem;
    }

    public void removerItem(ItemPedido item) {
        if (item == null) throw new RegraDeNegocioException("Item obrigatorio");
        if (this.status != StatusVersao.PENDENTE_APROVACAO) {
            throw new RegraDeNegocioException("So pode remover item em versao PENDENTE_APROVACAO");
        }
        if (!itens.contains(item)) throw new RegraDeNegocioException("Item nao pertence a versao");
        removerItemInterno(item);
    }

    public void removerItem(Long itemId) {
        if (itemId == null) throw new RegraDeNegocioException("Item obrigatorio");
        ItemPedido alvo = null;
        for (ItemPedido i : itens) {
            if (itemId.equals(i.getId())) { alvo = i; break; }
        }
        if (alvo == null) throw new RegraDeNegocioException("Item nao pertence a versao");
        if (this.status != StatusVersao.PENDENTE_APROVACAO) {
            throw new RegraDeNegocioException("So pode remover item em versao PENDENTE_APROVACAO");
        }
        removerItemInterno(alvo);
    }

    public void removerItem(int index) {
        if (index < 0 || index >= itens.size()) throw new RegraDeNegocioException("Indice invalido");
        if (this.status != StatusVersao.PENDENTE_APROVACAO) {
            throw new RegraDeNegocioException("So pode remover item em versao PENDENTE_APROVACAO");
        }
        removerItemInterno(itens.get(index));
    }

    public void alterarDescricao(String novaDescricao) {
        if (this.status != StatusVersao.PENDENTE_APROVACAO) {
            throw new RegraDeNegocioException("So pode alterar descricao em versao PENDENTE_APROVACAO");
        }
        if (novaDescricao != null) {
            this.descricaoPersonalizacao = novaDescricao.trim();
        }
    }

    ItemPedido adicionarItemInterno(ItemPedido item) {
        this.itens.add(item);
        return item;
    }

    void removerItemInterno(ItemPedido item) {
        this.itens.remove(item);
    }

    private static int nextNumero(Pedido pedido) {
        int n = 1;
        if (pedido != null) {
            for (VersaoPedido v : pedido.getVersoes()) {
                if (v.getNumero() >= n) {
                    n = v.getNumero() + 1;
                }
            }
        }
        return n;
    }
}