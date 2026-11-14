package br.com.encomendas.model;

import jakarta.persistence.*;
import br.com.encomendas.model.enums.StatusPedido;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "dias_producao", uniqueConstraints = @UniqueConstraint(columnNames = "data"))
public class DiaProducao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private LocalDate data;

    @Column(nullable = false)
    private int capacidade;

    @Column(name = "vagas_ocupadas", nullable = false)
    private int vagasOcupadas = 0;

    @OneToMany(mappedBy = "diaProducao", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Pedido> pedidos = new ArrayList<>();

    protected DiaProducao() {}

    public DiaProducao(LocalDate data, int capacidade) {
        if (data == null) {
            throw new RegraDeNegocioException("Data obrigatoria");
        }
        if (capacidade <= 0) {
            throw new RegraDeNegocioException("Capacidade deve ser maior que zero");
        }

        this.data = data;
        this.capacidade = capacidade;
    }

    public Long getId() { return id; }
    public LocalDate getData() { return data; }
    public int getCapacidade() { return capacidade; }
    public int getVagasOcupadas() { return vagasOcupadas(); }
    public int vagasOcupadas() {
        int total = this.vagasOcupadas;
        for (Pedido p : pedidos) {
            if (p != null && p.getStatus() != StatusPedido.CANCELADO) {
                total++;
            }
        }
        return total;
    }
    public int vagasLivres() { return capacidade - vagasOcupadas(); }
    public boolean temVagaLivre() { return vagasLivres() > 0; }

    public void reservar() {
        if (!temVagaLivre()) {
            throw new RegraDeNegocioException("Dia de producao lotado");
        }
        this.vagasOcupadas++;
    }

    public void liberar() {
        if (this.vagasOcupadas > 0) {
            this.vagasOcupadas--;
        }
    }

    public List<Pedido> getPedidos() { return Collections.unmodifiableList(pedidos); }

    public void ajustarCapacidade(int novaCapacidade) {
        if (novaCapacidade <= 0) {
            throw new RegraDeNegocioException("Capacidade deve ser maior que zero");
        }
        if (novaCapacidade < vagasOcupadas()) {
            throw new RegraDeNegocioException("Capacidade nao pode ficar abaixo das vagas ocupadas");
        }
        this.capacidade = novaCapacidade;
    }

    public void reservar(int quantidade) {
        if (quantidade <= 0) {
            throw new RegraDeNegocioException("Quantidade deve ser maior que zero");
        }
        if (quantidade > vagasLivres()) {
            throw new RegraDeNegocioException("Vagas insuficientes");
        }
        this.vagasOcupadas += quantidade;
    }

    public void liberar(int quantidade) {
        if (quantidade <= 0) {
            throw new RegraDeNegocioException("Quantidade deve ser maior que zero");
        }
        if (quantidade > this.vagasOcupadas) {
            throw new RegraDeNegocioException("Nao ha vagas suficientes para liberar");
        }
        this.vagasOcupadas -= quantidade;
    }

    void reservar(Pedido pedido) {
        if (pedido == null) {
            throw new RegraDeNegocioException("Pedido obrigatorio");
        }
        if (!temVagaLivre()) {
            throw new RegraDeNegocioException("Dia de producao lotado");
        }
        if (!pedidos.contains(pedido)) {
            pedidos.add(pedido);
        }
    }

    void liberar(Pedido pedido) {
        if (pedido == null) {
            throw new RegraDeNegocioException("Pedido obrigatorio");
        }
        pedidos.remove(pedido);
    }
}