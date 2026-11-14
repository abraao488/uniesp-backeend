# SPEC ATIVA — Camada Model (Segunda Unidade)

Projeto: gestao de encomendas de produtos personalizados.
Stack: Java 17, Spring Boot 3.x, Maven, Spring Data JPA, H2 em memoria.
Pacote base: `br.com.encomendas`.

Escopo desta etapa: **somente** `br.com.encomendas.model`.
Fora de escopo (nao criar agora): controllers, services, repositories, endpoints, configuracoes, seed de dados.

Regras de estilo obrigatorias:

- Modelo de dominio rico: sem setters genericos, estado alterado apenas por metodos com nome de negocio.
- Construtores validam dados e lancam `RegraDeNegocioException` (RuntimeException) quando a regra e violada.
- Nenhuma colecao exposta de forma mutavel: expor sempre `List.copyOf(...)` / `Collections.unmodifiableList(...)`.
- Sem Lombok. Sem dependencia nova no `pom.xml` (o pom ja existe e nao deve ser editado).
- Comentarios: somente Javadoc curto quando agregar; nunca comentario redundante.

## Contrato de assinaturas (ambos os lanes devem bater exatamente)

### Enums (pacote `br.com.encomendas.model`)

| Enum | Constantes |
|---|---|
| `CanalOrigem` | `WHATSAPP, INSTAGRAM, PRESENCIAL` |
| `PerfilFuncionario` | `ATENDIMENTO, PRODUCAO, ENTREGA, PROPRIETARIO` |
| `StatusPedido` | `RECEBIDO, AGUARDANDO_MATERIAL, EM_PRODUCAO, PRONTO, ENTREGUE, CANCELADO` |
| `StatusVersao` | `PENDENTE_APROVACAO, APROVADA, INVALIDADA` |
| `OrigemAprovacao` | `CLIENTE_VIA_LINK, ATENDENTE` |
| `TipoPagamento` | `SINAL, PARCIAL, RESTANTE` |
| `FormaPagamento` | `PIX, CARTAO, DINHEIRO` |

### `RegraDeNegocioException`

```java
public class RegraDeNegocioException extends RuntimeException {
    public RegraDeNegocioException(String mensagem) { super(mensagem); }
}
```

### Lote A — `Cliente`, `Funcionario`, `Produto`, `DiaProducao`, `LinkAcompanhamento`

```java
public class Cliente {
    public Cliente(Long id, String nome, String telefone, String email,
                   CanalOrigem canalOrigem, LocalDateTime dataCadastro);
    public Long getId();
    public String getNome();
    public String getTelefone();
    public String getEmail();
    public CanalOrigem getCanalOrigem();
    public LocalDateTime getDataCadastro();
}

public class Funcionario {
    public Funcionario(Long id, String nome, String email, PerfilFuncionario perfil, boolean ativo);
    public Long getId();
    public String getNome();
    public String getEmail();
    public PerfilFuncionario getPerfil();
    public boolean isAtivo();
    public void ativar();
    public void desativar();
}

public class Produto {
    public Produto(Long id, String nome, String descricao, BigDecimal precoBase, boolean ativo);
    public Long getId();
    public String getNome();
    public String getDescricao();
    public BigDecimal getPrecoBase();
    public boolean isAtivo();
    public void ativar();
    public void desativar();
    public void reajustarPreco(BigDecimal novoPreco);
}

public class DiaProducao {
    public DiaProducao(Long id, LocalDate data, int capacidade);
    public Long getId();
    public LocalDate getData();
    public int getCapacidade();
    public int getVagasOcupadas();
    public int vagasDisponiveis();
    public boolean temVaga();
    public void reservarVaga();
    public void liberarVaga();
}

public class LinkAcompanhamento {
    public LinkAcompanhamento(Long id, Pedido pedido);
    public Long getId();
    public String getToken();
    public boolean isAtivo();
    public LocalDateTime getCriadoEm();
    public Pedido getPedido();
    public void desativar();
    public void reativar();
}
```

### Lote B — `Pedido`, `VersaoPedido`, `ItemPedido`, `Pagamento`

```java
public class Pedido {
    public Pedido(Long id, Cliente cliente, Funcionario atendente,
                  LocalDate dataEntrega, DiaProducao diaProducao);
    public Long getId();
    public Cliente getCliente();
    public Funcionario getAtendente();
    public StatusPedido getStatus();
    public LocalDateTime getDataCriacao();
    public LocalDate getDataEntrega();
    public DiaProducao getDiaProducao();
    public List<VersaoPedido> getVersoes();
    public List<Pagamento> getPagamentos();
    public LinkAcompanhamento getLinkAcompanhamento();
    public LinkAcompanhamento gerarLinkAcompanhamento();
    public VersaoPedido adicionarVersao(int numero, String descricaoPersonalizacao, Funcionario criadaPor);
    public void aprovarVersao(int numero, Funcionario aprovador, OrigemAprovacao origem);
    public VersaoPedido getVersaoVigente();
    public ItemPedido adicionarItem(int numeroVersao, Produto produto, int quantidade,
                                    BigDecimal precoUnitario, String personalizacao);
    public void registrarPagamento(Pagamento pagamento);
    public BigDecimal getValorTotal();
    public BigDecimal getTotalPago();
    public BigDecimal getSaldoDevedor();
    public boolean isSaldoQuitado();
    public void aguardarMaterial();
    public void iniciarProducao();
    public void marcarPronto();
    public void entregar();
    public void cancelar();
}

public class VersaoPedido {
    public VersaoPedido(Long id, int numero, String descricaoPersonalizacao,
                        Funcionario criadaPor, LocalDateTime criadaEm);
    public Long getId();
    public int getNumero();
    public String getDescricaoPersonalizacao();
    public StatusVersao getStatus();
    public LocalDateTime getCriadaEm();
    public LocalDateTime getAprovadaEm();
    public Funcionario getCriadaPor();
    public Funcionario getAprovador();
    public OrigemAprovacao getOrigemAprovacao();
    public List<ItemPedido> getItens();
    public ItemPedido adicionarItem(Produto produto, int quantidade,
                                    BigDecimal precoUnitario, String personalizacao);
    public BigDecimal getValorTotal();
    public boolean isAprovada();
    public void aprovar(Funcionario aprovador, OrigemAprovacao origem);
    public void invalidar();
}

public class ItemPedido {
    public ItemPedido(Long id, Produto produto, int quantidade,
                      BigDecimal precoUnitario, String personalizacao);
    public Long getId();
    public Produto getProduto();
    public int getQuantidade();
    public BigDecimal getPrecoUnitario();
    public String getPersonalizacao();
    public BigDecimal getSubtotal();
}

public class Pagamento {
    public Pagamento(Long id, BigDecimal valor, TipoPagamento tipo, FormaPagamento forma,
                     LocalDateTime dataPagamento, Funcionario registradoPor);
    public Long getId();
    public BigDecimal getValor();
    public TipoPagamento getTipo();
    public FormaPagamento getForma();
    public LocalDateTime getDataPagamento();
    public Funcionario getRegistradoPor();
}
```

## Regras de negocio do contrato

1. `Cliente`: `nome`, `telefone`, `email` obrigatorios e nao vazios; `email` deve conter `@`; `canalOrigem` e `dataCadastro` obrigatorios.
2. `Funcionario`: `nome`, `email` obrigatorios; `email` com `@`; `perfil` obrigatorio. `ativar`/`desativar` idempotentes.
3. `Produto`: `nome` obrigatorio; `precoBase` obrigatorio e maior que zero; `reajustarPreco` rejeita valor nulo, zero ou negativo.
4. `DiaProducao`: `data` obrigatoria; `capacidade` maior que zero. `reservarVaga` lanca `RegraDeNegocioException` quando nao ha vaga (Regra 2). `liberarVaga` nunca deixa o contador negativo. `getVagasOcupadas()` parte de zero.
5. `LinkAcompanhamento`: `token` gerado com `UUID.randomUUID().toString()`; `criadoEm` na criacao; `pedido` obrigatorio.
6. `Pedido`: `cliente`, `atendente`, `dataEntrega` e `diaProducao` obrigatorios; `atendente` precisa estar ativo; `dataCriacao = LocalDateTime.now()`; `status` inicial `RECEBIDO`; no construtor reserva vaga no `diaProducao` (Regra 2: dia cheio nao aceita pedido novo).
7. Versoes: `adicionarVersao` exige numero positivo e unico no pedido, `criadaPor` obrigatorio e ativo; pedido `ENTREGUE` ou `CANCELADO` nao recebe versao nova.
8. Aprovacao: `aprovarVersao` exige versao `PENDENTE_APROVACAO`, `aprovador` obrigatorio, `origem` obrigatoria; aprovacao dispara `invalidar()` nas versoes aprovadas anteriores (Regra 1: producao trabalha so com a ultima versao aprovada; antigas ficam `INVALIDADA`).
9. Itens: quantidade e `precoUnitario` maiores que zero; `produto` obrigatorio; item so entra em versao `PENDENTE_APROVACAO` (versao aprovada/invalidada e imutavel). `getSubtotal() = precoUnitario * quantidade`.
10. Pagamentos: `valor` maior que zero; `tipo`, `forma` e `dataPagamento` obrigatorios; `registradoPor` obrigatorio. `Pagamento` e imutavel depois de criado. Registro bloqueado para pedido `CANCELADO`.
11. Valores: `getValorTotal()` soma os itens da versao vigente (zero quando nao houver versao aprovada); `getTotalPago()` soma os pagamentos; `getSaldoDevedor()` nunca negativo; `isSaldoQuitado()` verdadeiro quando o saldo e zero.
12. Status: `aguardarMaterial` (`RECEBIDO` -> `AGUARDANDO_MATERIAL`), `iniciarProducao` (`RECEBIDO`/`AGUARDANDO_MATERIAL` -> `EM_PRODUCAO`), `marcarPronto` (`EM_PRODUCAO` -> `PRONTO`, exige versao aprovada), `entregar` (`PRONTO` -> `ENTREGUE`, exige saldo zero, Regra 3; libera a vaga do dia), `cancelar` (qualquer status != `ENTREGUE` -> `CANCELADO`; libera a vaga do dia). Toda transicao invalida lanca `RegraDeNegocioException`.
13. `getVersoes()`, `getPagamentos()` e `getItens()` retornam listas imutaveis; `getVersaoVigente()` retorna a versao `APROVADA` mais recente ou `null`.

## Divisao de trabalho

- Lote A (agente `omp`): enums, `RegraDeNegocioException`, `Cliente`, `Funcionario`, `Produto`, `DiaProducao`, `LinkAcompanhamento`.
- Lote B (agente `pi`): `Pedido`, `VersaoPedido`, `ItemPedido`, `Pagamento`.

Cada agente cria somente os arquivos do seu lote. Nenhum agente edita `pom.xml`, arquivos do outro lote ou fora de `src/main/java/br/com/encomendas/model/`.

## Verificacao

Nao ha JDK/Maven instalados nesta maquina: a compilacao real fica pendente. A verificacao de integracao e revisao estatica do contrato (nomes, assinaturas, excecoes, imutabilidade) e registro da pendencia em `DEV/VERIFY.md`.