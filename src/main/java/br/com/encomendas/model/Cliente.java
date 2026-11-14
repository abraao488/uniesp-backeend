package br.com.encomendas.model;

import br.com.encomendas.model.enums.CanalOrigem;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "clientes")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, length = 20)
    private String telefone;

    @Column(nullable = false, length = 150)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "canal_origem", nullable = false, length = 20)
    private CanalOrigem canalOrigem;

    @Column(name = "data_cadastro", nullable = false, updatable = false)
    private LocalDateTime dataCadastro;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Pedido> pedidos = new ArrayList<>();

    protected Cliente() {}

    public Cliente(String nome, String telefone, String email, CanalOrigem canalOrigem, LocalDateTime dataCadastro) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new RegraDeNegocioException("Nome obrigatorio");
        }
        if (telefone == null || telefone.trim().isEmpty()) {
            throw new RegraDeNegocioException("Telefone obrigatorio");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new RegraDeNegocioException("Email obrigatorio");
        }
        if (!email.contains("@")) {
            throw new RegraDeNegocioException("Email invalido");
        }
        if (canalOrigem == null) {
            throw new RegraDeNegocioException("Canal de origem obrigatorio");
        }
        if (dataCadastro == null) {
            throw new RegraDeNegocioException("Data de cadastro obrigatoria");
        }

        this.nome = nome.trim();
        this.telefone = telefone.trim();
        this.email = email.trim();
        this.canalOrigem = canalOrigem;
        this.dataCadastro = dataCadastro;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getTelefone() { return telefone; }
    public String getEmail() { return email; }
    public CanalOrigem getCanalOrigem() { return canalOrigem; }
    public LocalDateTime getDataCadastro() { return dataCadastro; }
    public List<Pedido> getPedidos() { return Collections.unmodifiableList(pedidos); }

    public void atualizarContato(String novoTelefone, String novoEmail) {
        if (novoTelefone == null || novoTelefone.trim().isEmpty()) {
            throw new RegraDeNegocioException("Telefone obrigatorio");
        }
        if (novoEmail == null || novoEmail.trim().isEmpty()) {
            throw new RegraDeNegocioException("Email obrigatorio");
        }
        if (!novoEmail.contains("@")) {
            throw new RegraDeNegocioException("Email invalido");
        }
        this.telefone = novoTelefone.trim();
        this.email = novoEmail.trim();
    }

    public void renomear(String novoNome) {
        if (novoNome == null || novoNome.trim().isEmpty()) {
            throw new RegraDeNegocioException("Nome obrigatorio");
        }
        this.nome = novoNome.trim();
    }
}