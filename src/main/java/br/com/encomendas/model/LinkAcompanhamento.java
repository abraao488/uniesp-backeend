package br.com.encomendas.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "links_acompanhamento", uniqueConstraints = @UniqueConstraint(columnNames = "token"))
public class LinkAcompanhamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 36)
    private String token;

    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false, unique = true)
    private Pedido pedido;

    protected LinkAcompanhamento() {}

    public LinkAcompanhamento(Pedido pedido) {
        if (pedido == null) {
            throw new RegraDeNegocioException("Pedido obrigatorio");
        }
        this.token = UUID.randomUUID().toString();
        this.ativo = true;
        this.criadoEm = LocalDateTime.now();
        this.pedido = pedido;
    }

    public Long getId() { return id; }
    public String getToken() { return token; }
    public boolean isAtivo() { return ativo; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public Pedido getPedido() { return pedido; }

    public void desativar() { this.ativo = false; }
    public void reativar() { this.ativo = true; }
    public boolean isValido() { return ativo; }

    public void regenerarToken() {
        this.token = java.util.UUID.randomUUID().toString();
    }
}