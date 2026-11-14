package br.com.encomendas.model;

import br.com.encomendas.model.enums.PerfilFuncionario;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "funcionarios")
public class Funcionario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, length = 150, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PerfilFuncionario perfil;

    @Column(nullable = false)
    private boolean ativo = true;

    @OneToMany(mappedBy = "atendente", fetch = FetchType.LAZY)
    private List<Pedido> pedidosAtendidos = new ArrayList<>();

    @OneToMany(mappedBy = "criadaPor", fetch = FetchType.LAZY)
    private List<VersaoPedido> versoesCriadas = new ArrayList<>();

    @OneToMany(mappedBy = "aprovador", fetch = FetchType.LAZY)
    private List<VersaoPedido> versoesAprovadas = new ArrayList<>();

    @OneToMany(mappedBy = "registradoPor", fetch = FetchType.LAZY)
    private List<Pagamento> pagamentosRegistrados = new ArrayList<>();

    protected Funcionario() {}

    public Funcionario(String nome, String email, PerfilFuncionario perfil, boolean ativo) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new RegraDeNegocioException("Nome obrigatorio");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new RegraDeNegocioException("Email obrigatorio");
        }
        if (!email.contains("@")) {
            throw new RegraDeNegocioException("Email invalido");
        }
        if (perfil == null) {
            throw new RegraDeNegocioException("Perfil obrigatorio");
        }

        this.nome = nome.trim();
        this.email = email.trim();
        this.perfil = perfil;
        this.ativo = ativo;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public PerfilFuncionario getPerfil() { return perfil; }
    public boolean isAtivo() { return ativo; }
    public void ativar() { this.ativo = true; }
    public void desativar() { this.ativo = false; }
    public void reativar() { this.ativo = true; }
    public List<Pedido> getPedidosAtendidos() { return Collections.unmodifiableList(pedidosAtendidos); }
    public List<VersaoPedido> getVersoesCriadas() { return Collections.unmodifiableList(versoesCriadas); }
    public List<VersaoPedido> getVersoesAprovadas() { return Collections.unmodifiableList(versoesAprovadas); }
    public List<Pagamento> getPagamentosRegistrados() { return Collections.unmodifiableList(pagamentosRegistrados); }

    public void alterarPerfil(PerfilFuncionario novoPerfil) {
        if (novoPerfil == null) {
            throw new RegraDeNegocioException("Perfil obrigatorio");
        }
        this.perfil = novoPerfil;
    }

    public boolean podeAtender() {
        return this.ativo && (this.perfil == PerfilFuncionario.ATENDIMENTO
                || this.perfil == PerfilFuncionario.PROPRIETARIO);
    }

    public boolean podeEntregar() {
        return this.ativo && (this.perfil == PerfilFuncionario.ENTREGA
                || this.perfil == PerfilFuncionario.PROPRIETARIO);
    }
}