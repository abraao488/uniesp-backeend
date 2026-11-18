# Gestão de Encomendas

Sistema de gestão de encomendas de produtos personalizados para uma pequena empresa.
Backend em Java 17 com Spring Boot, JPA/H2 e testes JUnit 5.

## 1. Descrição do projeto e problema da empresa

A empresa produz e entrega produtos personalizados (camisetas bordadas, brindes, etc.).
O problema hoje é que alterações de versão dos pedidos não chegam à produção, os prazos
não levam em conta a capacidade de produção (dia agendado cheio aceita novos agendamentos)
e as entregas são feitas sem conferir se o cliente já quitou o pagamento.

O sistema resolve esses três problemas com regras de domínio aplicadas diretamente no
modelo, impedindo erros antes que cheguem ao banco de dados.

## 2. A solução

Três regras de negócio implementadas na camada de domínio (modelo rico), mais um
recurso de acompanhamento:

| Regra | O que faz |
|-------|-----------|
| **Versão aprovada** | Uma versão só vira vigente depois de aprovada. Uma nova aprovação invalida a anterior (a antiga passa para `INVALIDADA`). |
| **Produção controlada** | Produção não inicia sem versão aprovada nem sem dia de produção agendado com vaga livre. |
| **Entrega condicionada ao pagamento** | Entrega é bloqueada enquanto houver saldo pendente e liberada somente após o pagamento total. |
| **Link de acompanhamento** | Geração de link público para o cliente acompanhar o status do pedido. |

## 3. Arquitetura escolhida: em camadas

O projeto segue uma arquitetura monolítica em camadas:

- `controller` — camada de entrada (REST)
- `service` — camada de regras de negócio e orquestração
- `repository` — camada de persistência (Spring Data JPA)
- `model` — camada de domínio (entidades, enums, value objects, exceções)

Justificativa por três critérios:

1. **Necessidades do negócio** — sistema único para uma pequena empresa, orçamento baixo
   e regras simples de impedir erro. Não há necessidade de distribuir responsabilidade
   entre serviços nem de isolar o domínio por portas e adaptadores.
2. **Complexidade do sistema** — domínio pequeno, único módulo implantável (um único
   JAR executável com banco H2 embutido). Uma arquitetura hexagonal adicionaria camadas
   de tradução (portas, adaptadores, DTOs) sem retorno proporcional neste tamanho.
3. **Características da equipe** — 2 estudantes que já conhecem Spring Boot e o padrão
   em camadas é o mais conhecido da equipe. Reduz a curva de aprendizado e facilita o
   trabalho em grupo.

**Por que não microsserviços agora:** overhead operacional (rede, descoberta,
implantação de múltiplos serviços) desproporcional para um sistema de porte pequeno.
**Por que não hexagonal agora:** o domínio é simples e não justifica a abstração de
camadas de infraestrutura; pode ser evoluída futuramente se o sistema crescer.

## 4. Organização do código

```
src/main/java/br/com/encomendas/
├── EncomendasApplication.java   (classe principal Spring Boot)
├── model/                       ← implementado nesta etapa
│   ├── Pedido.java              (entidade agregada raiz)
│   ├── VersaoPedido.java        (entidade)
│   ├── ItemPedido.java          (entidade)
│   ├── Pagamento.java           (entidade)
│   ├── Cliente.java             (entidade)
│   ├── Funcionario.java         (entidade)
│   ├── DiaProducao.java         (entidade)
│   ├── LinkAcompanhamento.java  (entidade)
│   ├── RegraDeNegocioException.java  (exceção de domínio)
│   ├── enums/                   (StatusPedido, StatusVersao, FormaPagamento,
│   │                            TipoPagamento, OrigemAprovacao, PerfilFuncionario,
│   │                            CanalOrigem)
│   ├── exception/
│   ├── repository/              ← próxima etapa (Spring Data JPA)
│   ├── service/                 ← próxima etapa (regras de aplicação)
│   └── controller/              ← próxima etapa (endpoints REST)
```

**O que já está implementado nesta etapa:** a camada `model` completa com as entidades,
enums e exceções de domínio, incluindo as regras de negócio (versão vigente, produção
controlada, entrega condicionada ao pagamento) e os testes JUnit 5 puros em
`src/test/java/br/com/encomendas/model/`.

**O que vem depois:** camadas `repository`, `service` e `controller`, seguidas de
integração com banco e testes de integração.

## 5. Entidades e relacionamentos

| Entidade | Relacionamentos |
|----------|-----------------|
| `Pedido` | 1:N com `VersaoPedido`, 1:N com `Pagamento`, 1:1 com `LinkAcompanhamento`, N:1 com `Cliente`, N:1 com `Funcionario` (atendente), N:1 com `DiaProducao` |
| `VersaoPedido` | N:1 com `Pedido`, 1:N com `ItemPedido`, N:1 com `Funcionario` (criada por e aprovador) |
| `ItemPedido` | N:1 com `VersaoPedido`, N:1 com `Produto` |
| `Pagamento` | N:1 com `Pedido`, N:1 com `Funcionario` (registrado por) |
| `Cliente` | 1:N com `Pedido` |
| `Funcionario` | 1:N com `Pedido` (atendente), 1:N com `VersaoPedido` (criador/aprovador), 1:N com `Pagamento` |
| `DiaProducao` | 1:N com `Pedido` |
| `LinkAcompanhamento` | 1:1 com `Pedido` |

`Pedido` é a agregada raiz: versões, pagamentos, itens e link de acompanhamento só
existem no contexto de um pedido.

## 6. Como executar

Requisitos: Java 17 e Maven.

```bash
# Rodar a aplicação
mvn spring-boot:run

# Acessar console H2 (http://localhost:8080/h2-console)
# JDBC URL: jdbc:h2:mem:testdb
```

```bash
# Rodar os testes unitários (JUnit 5, sem Spring)
mvn test -Dtest=PedidoTest

# Rodar todos os testes
mvn test
```

## 7. Diagrama e integrantes

- Diagrama de classes: `docs/diagrama-de-classes.svg` (e `.png`, `.puml`)
- Integrantes: Abraão e Vitor Rodrigo.
