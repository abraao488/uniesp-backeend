package br.com.encomendas.model;

import br.com.encomendas.model.enums.FormaPagamento;
import br.com.encomendas.model.enums.OrigemAprovacao;
import br.com.encomendas.model.enums.StatusPedido;
import br.com.encomendas.model.enums.StatusVersao;
import br.com.encomendas.model.enums.TipoPagamento;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "pedidos")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atendente_id", nullable = false)
    private Funcionario atendente;

    @Column(name = "data_entrega", nullable = false)
    private LocalDate dataEntrega;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dia_producao_id", nullable = false)
    private DiaProducao diaProducao;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusPedido status = StatusPedido.RECEBIDO;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("numero ASC")
    private List<VersaoPedido> versoes = new ArrayList<>();

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Pagamento> pagamentos = new ArrayList<>();

    @OneToOne(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private LinkAcompanhamento linkAcompanhamento;

    protected Pedido() {}

    public Pedido(Cliente cliente, Funcionario atendente, LocalDate dataEntrega, DiaProducao diaProducao) {
        if (cliente == null) throw new RegraDeNegocioException("Cliente obrigatorio");
        if (atendente == null) throw new RegraDeNegocioException("Atendente obrigatorio");
        if (!atendente.isAtivo()) throw new RegraDeNegocioException("Atendente nao esta ativo");
        if (dataEntrega == null) throw new RegraDeNegocioException("Data de entrega obrigatoria");
        if (diaProducao == null) throw new RegraDeNegocioException("Dia de producao obrigatorio");
        if (!diaProducao.temVagaLivre()) throw new RegraDeNegocioException("Dia de producao lotado");

        this.cliente = cliente;
        this.atendente = atendente;
        this.dataEntrega = dataEntrega;
        this.diaProducao = diaProducao;
        this.dataCriacao = LocalDateTime.now();
        this.status = StatusPedido.RECEBIDO;

        diaProducao.reservar();
    }

    public Pedido(Cliente cliente, Funcionario atendente) {
        if (cliente == null) throw new RegraDeNegocioException("Cliente obrigatorio");
        if (atendente == null) throw new RegraDeNegocioException("Atendente obrigatorio");
        if (!atendente.isAtivo()) throw new RegraDeNegocioException("Atendente nao esta ativo");

        this.cliente = cliente;
        this.atendente = atendente;
        this.dataCriacao = LocalDateTime.now();
        this.status = StatusPedido.RECEBIDO;

        this.linkAcompanhamento = new LinkAcompanhamento(this);
        setLinkAcompanhamentoInterno(this.linkAcompanhamento);
    }

    public Long getId() { return id; }
    public Cliente getCliente() { return cliente; }
    public Funcionario getAtendente() { return atendente; }
    public LocalDate getDataEntrega() { return dataEntrega; }
    public DiaProducao getDiaProducao() { return diaProducao; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public StatusPedido getStatus() { return status; }
    public List<VersaoPedido> getVersoes() { return Collections.unmodifiableList(versoes); }
    public List<Pagamento> getPagamentos() { return Collections.unmodifiableList(pagamentos); }
    public LinkAcompanhamento getLinkAcompanhamento() { return linkAcompanhamento; }

    VersaoPedido adicionarVersaoInterno(VersaoPedido versao) {
        this.versoes.add(versao);
        return versao;
    }

    void removerVersaoInterno(VersaoPedido versao) {
        this.versoes.remove(versao);
    }

    void adicionarPagamentoInterno(Pagamento pagamento) {
        this.pagamentos.add(pagamento);
    }

    void setLinkAcompanhamentoInterno(LinkAcompanhamento link) {
        this.linkAcompanhamento = link;
    }

    void liberarVagaDiaProducao() {
        if (diaProducao != null) {
            diaProducao.liberar(this);
            diaProducao.liberar();
        }
    }

    private VersaoPedido findVersao(int numero) {
        for (VersaoPedido v : versoes) {
            if (v.getNumero() == numero) return v;
        }
        throw new RegraDeNegocioException("Versao nao encontrada");
    }

    public VersaoPedido criarVersao(int numero, String descricaoPersonalizacao, Funcionario criadaPor) {
        if (status == StatusPedido.ENTREGUE || status == StatusPedido.CANCELADO) {
            throw new RegraDeNegocioException("Pedido nao aceita nova versao");
        }
        if (numero <= 0) throw new RegraDeNegocioException("Numero da versao deve ser positivo");
        for (VersaoPedido v : versoes) {
            if (v.getNumero() == numero) throw new RegraDeNegocioException("Numero de versao duplicado");
        }
        if (criadaPor == null) throw new RegraDeNegocioException("Funcionario criadaPor obrigatorio");
        if (!criadaPor.isAtivo()) throw new RegraDeNegocioException("Funcionario criadaPor deve estar ativo");
        return new VersaoPedido(this, numero, descricaoPersonalizacao, criadaPor, LocalDateTime.now());
    }

    public VersaoPedido criarVersao(String descricao, Funcionario autor) {
        if (status == StatusPedido.ENTREGUE || status == StatusPedido.CANCELADO) {
            throw new RegraDeNegocioException("Pedido nao aceita nova versao");
        }
        if (autor == null) throw new RegraDeNegocioException("Funcionario criadaPor obrigatorio");
        if (!autor.isAtivo()) throw new RegraDeNegocioException("Funcionario criadaPor deve estar ativo");

        for (VersaoPedido v : versoes) {
            if (v.getStatus() == StatusVersao.PENDENTE_APROVACAO) {
                v.invalidar();
            }
        }

        VersaoPedido fonte = getVersaoVigente();
        if (fonte == null && !versoes.isEmpty()) {
            fonte = versoes.get(versoes.size() - 1);
        }

        int numero = 1;
        for (VersaoPedido v : versoes) {
            if (v.getNumero() >= numero) {
                numero = v.getNumero() + 1;
            }
        }

        VersaoPedido nova = new VersaoPedido(this, numero, descricao, autor, LocalDateTime.now());

        if (fonte != null) {
            for (ItemPedido item : fonte.getItens()) {
                nova.adicionarItem(item.getProduto(), item.getQuantidade(), item.getPrecoUnitario(), item.getPersonalizacao());
            }
        }

        return nova;
    }

    public void aprovarVersao(int numero, Funcionario aprovador, OrigemAprovacao origem) {
        if (origem == null) throw new RegraDeNegocioException("Origem da aprovacao obrigatoria");
        VersaoPedido versao = findVersao(numero);
        versao.aprovar(aprovador, origem);
        for (VersaoPedido v : versoes) {
            if (v != versao && v.isAprovada()) {
                v.invalidar();
            }
        }
    }

    public void aprovarVersaoPeloCliente(int numero) {
        aprovarVersao(numero, null, OrigemAprovacao.CLIENTE_VIA_LINK);
    }

    public void aprovarVersaoPeloCliente(int numero, Funcionario aprovador) {
        aprovarVersao(numero, aprovador, OrigemAprovacao.CLIENTE_VIA_LINK);
    }

    public void aprovarVersaoInternamente(int numero, Funcionario aprovador) {
        aprovarVersao(numero, aprovador, OrigemAprovacao.ATENDENTE);
    }

    public void aprovarVersaoPeloCliente(VersaoPedido versao) {
        aprovarVersao(versao, null, OrigemAprovacao.CLIENTE_VIA_LINK);
    }

    public void aprovarVersaoInternamente(VersaoPedido versao, Funcionario aprovador) {
        aprovarVersao(versao, aprovador, OrigemAprovacao.ATENDENTE);
    }

    public void aprovarVersao(VersaoPedido versao, Funcionario aprovador, OrigemAprovacao origem) {
        if (versao == null) throw new RegraDeNegocioException("Versao obrigatoria");
        if (versao.getPedido() != this) throw new RegraDeNegocioException("Versao nao pertence ao pedido");
        if (origem == null) throw new RegraDeNegocioException("Origem da aprovacao obrigatoria");
        if (aprovador == null && origem != OrigemAprovacao.CLIENTE_VIA_LINK) {
            throw new RegraDeNegocioException("Aprovador obrigatorio");
        }

        versao.aprovar(aprovador, origem);

        for (VersaoPedido v : versoes) {
            if (v != versao && v.isAprovada()) {
                v.invalidar();
            }
        }
    }

    public VersaoPedido getVersaoVigente() {
        VersaoPedido vigente = null;
        for (VersaoPedido v : versoes) {
            if (v.isAprovada()) vigente = v;
        }
        return vigente;
    }

    public ItemPedido adicionarItem(int numeroVersao, Produto produto, int quantidade, BigDecimal precoUnitario, String personalizacao) {
        VersaoPedido versao = findVersao(numeroVersao);
        return new ItemPedido(versao, produto, quantidade, precoUnitario, personalizacao);
    }

    public void registrarPagamento(Pagamento pagamento) {
        if (pagamento == null) throw new RegraDeNegocioException("Pagamento obrigatorio");
        if (status == StatusPedido.CANCELADO) throw new RegraDeNegocioException("Registro bloqueado para pedido CANCELADO");
        pagamentos.remove(pagamento);
        if (pagamento.getValor().compareTo(getSaldoDevedor()) > 0) {
            throw new RegraDeNegocioException("Pagamento excede o saldo devedor");
        }
        if (!pagamentos.contains(pagamento)) {
            adicionarPagamentoInterno(pagamento);
        }
    }

    public Pagamento registrarPagamento(BigDecimal valor, TipoPagamento tipo, FormaPagamento forma, Funcionario registradoPor) {
        if (getVersaoVigente() == null) {
            throw new RegraDeNegocioException("Versao aprovada obrigatoria");
        }
        BigDecimal saldo = getSaldoDevedor();
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraDeNegocioException("Valor deve ser maior que zero");
        }
        if (valor.compareTo(saldo) > 0) {
            throw new RegraDeNegocioException("Valor maior que o saldo devedor");
        }
        return new Pagamento(this, valor, tipo, forma, LocalDateTime.now(), registradoPor);
    }

    public BigDecimal getValorTotal() {
        VersaoPedido vigente = getVersaoVigente();
        return vigente == null ? BigDecimal.ZERO : vigente.getValorTotal();
    }

    public BigDecimal getTotalPago() {
        BigDecimal total = BigDecimal.ZERO;
        for (Pagamento p : pagamentos) total = total.add(p.getValor());
        return total;
    }

    public BigDecimal getSaldoDevedor() {
        BigDecimal saldo = getValorTotal().subtract(getTotalPago());
        return saldo.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : saldo;
    }

    public VersaoPedido versaoVigente() { return getVersaoVigente(); }
    public BigDecimal valorTotal() { return getValorTotal(); }
    public BigDecimal totalPago() { return getTotalPago(); }
    public BigDecimal saldoDevedor() { return getSaldoDevedor(); }

    public boolean isSaldoQuitado() {
        return getSaldoDevedor().compareTo(BigDecimal.ZERO) == 0;
    }

    public LinkAcompanhamento gerarLinkAcompanhamento() {
        if (linkAcompanhamento == null) {
            linkAcompanhamento = new LinkAcompanhamento(this);
            setLinkAcompanhamentoInterno(linkAcompanhamento);
        }
        return linkAcompanhamento;
    }

    public void marcarAguardandoMaterial() {
        transicionarPara(StatusPedido.AGUARDANDO_MATERIAL);
    }

    public void iniciarProducao() {
        if (getVersaoVigente() == null) {
            throw new RegraDeNegocioException("Versao aprovada obrigatoria");
        }
        if (diaProducao == null) {
            throw new RegraDeNegocioException("Dia de producao nao agendado");
        }
        transicionarPara(StatusPedido.EM_PRODUCAO);
    }

    public void marcarPronto() {
        if (getVersaoVigente() == null) throw new RegraDeNegocioException("Versao aprovada obrigatoria");
        transicionarPara(StatusPedido.PRONTO);
    }

    public void agendarEntrega(DiaProducao novoDia) {
        if (novoDia == null) {
            throw new RegraDeNegocioException("Dia de producao obrigatorio");
        }
        if (!novoDia.temVagaLivre()) {
            throw new RegraDeNegocioException("Dia de producao lotado");
        }
        DiaProducao anterior = this.diaProducao;
        if (anterior == null || anterior != novoDia) {
            novoDia.reservar(this);
        }
        if (anterior != null && anterior != novoDia) {
            if (!anterior.getPedidos().contains(this)) {
                anterior.liberar();
            }
            anterior.liberar(this);
        }
        this.diaProducao = novoDia;
        if (this.dataEntrega == null) {
            this.dataEntrega = novoDia.getData();
        }
    }

    public void agendarEntrega(LocalDate novaDataEntrega) {
        if (novaDataEntrega == null) throw new RegraDeNegocioException("Data de entrega obrigatoria");
        this.dataEntrega = novaDataEntrega;
    }

    public void agendarEntrega(LocalDate novaDataEntrega, DiaProducao novoDia) {
        if (novaDataEntrega == null) throw new RegraDeNegocioException("Data de entrega obrigatoria");
        if (novoDia != null && novoDia != diaProducao && !novoDia.temVagaLivre()) {
            throw new RegraDeNegocioException("Dia de producao lotado");
        }
        this.dataEntrega = novaDataEntrega;
        if (novoDia != null && novoDia != diaProducao) {
            liberarVagaDiaProducao();
            this.diaProducao = novoDia;
            novoDia.reservar();
        }
    }

    public void registrarEntrega() {
        registrarEntrega(null);
    }

    public void registrarEntrega(Funcionario entregador) {
        if (!status.podeIrPara(StatusPedido.ENTREGUE)) {
            throw new RegraDeNegocioException("Transicao invalida para ENTREGUE");
        }
        BigDecimal saldo = getSaldoDevedor();
        if (saldo.compareTo(BigDecimal.ZERO) > 0) {
            throw new RegraDeNegocioException("Saldo pendente: " + saldo);
        }
        status = StatusPedido.ENTREGUE;
        liberarVagaDiaProducao();
    }

    public void cancelar() {
        if (!status.podeIrPara(StatusPedido.CANCELADO)) {
            throw new RegraDeNegocioException("Nao pode cancelar pedido " + status);
        }
        status = StatusPedido.CANCELADO;
        liberarVagaDiaProducao();
    }

    public VersaoPedido adicionarVersao(int numero, String descricaoPersonalizacao, Funcionario criadaPor) {
        return criarVersao(numero, descricaoPersonalizacao, criadaPor);
    }

    public void aguardarMaterial() {
        marcarAguardandoMaterial();
    }

    public void entregar() {
        registrarEntrega();
    }

    private void transicionarPara(StatusPedido destino) {
        if (!status.podeIrPara(destino)) {
            throw new RegraDeNegocioException("Transicao invalida de " + status + " para " + destino);
        }
        status = destino;
    }
}
