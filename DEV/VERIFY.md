# Verificação — Métodos de estado do domínio (task_a2d7acabb733)

Data: 2026-11-14.

## Compilação e testes

Máquina não possui JDK/Maven; instalados em `~AppData/Local/tools`:
- JDK 17 (Temurin 17.0.21+9)
- Maven 3.9.6

`mvn clean test` (Java 17, Spring Boot 3.3.5):

```
Tests run: 72, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Classes de teste: `ClienteTest` (5), `FuncionarioTest` (6), `ProdutoTest` (6),
`DiaProducaoTest` (11), `LinkAcompanhamentoTest` (4), `StatusPedidoTest` (6),
`PedidoTest` (25), `VersaoPedidoTest` (6), `ItemPedidoTest` (3) + `ModelFixtures`.

## Métodos implementados (todos com teste unitário)

| Entidade | Métodos |
|---|---|
| Cliente | `atualizarContato`, `renomear` |
| Funcionario | `alterarPerfil`, `desativar`, `reativar`, `podeAtender`, `podeEntregar` |
| Produto | `alterarPreco`, `alterarDescricao`, `desativar`, `reativar` |
| DiaProducao | `vagasLivres`, `temVagaLivre`, `ajustarCapacidade`, `reservar()`, `reservar(int)`, `liberar()`, `liberar(int)` (getVagasOcupadas já existia) |
| StatusPedido | `podeIrPara` (estado final, transição própria e null = inválido) |
| Pedido | `criarVersao`, `aprovarVersaoPeloCliente`, `aprovarVersaoInternamente`, `registrarPagamento`, `agendarEntrega` (1 arg), `agendarEntrega` (data+dia), `marcarAguardandoMaterial`, `iniciarProducao`, `marcarPronto`, `registrarEntrega`, `cancelar` |
| VersaoPedido | `adicionarItem`, `removerItem`, `alterarDescricao` |
| ItemPedido | `alterarQuantidade` |
| LinkAcompanhamento | `desativar`, `reativar`, `regenerarToken` |

Validação de nulos/vazios/inválidos com `RegraDeNegocioException` em todos.

## Reparações em arquivos pré-existentes (estavam quebrados)

- `ItemPedido.java`: fragmento duplicado após o fechamento da classe removido
  (arquivo não compilava).
- `VersaoPedido.java`: classe fechada no meio do `removerItem(int)`; `alterarDescricao`
  e `adicionarItemInterno` ausentes — reconstituídos (arquivo não compilava).

## Consolidações feitas

- Transições de status do `Pedido` agora passam por `StatusPedido.podeIrPara`
  (guarda central); removido `Pedido.setStatus` (setter genérico) e duplicados
  `aguardarMaterial`/`entregar`/`adicionarVersao`.
- `DiaProducao`: métodos legados `temVaga`/`reservarVaga`/`liberarVaga`/
  `vagasDisponiveis` removidos; somente as formas de nome do contrato
  (`vagasLivres`, `temVagaLivre`, `reservar`, `liberar`) permanecem.
  `ajustarCapacidade` agora rejeita capacidade menor que as vagas ocupadas.
- `Produto`: `reajustarPreco` (duplicado) removido; mantido `alterarPreco`.
- `Pedido.agendarEntrega(data, dia)` valida o novo dia antes de qualquer mutação.

## Restos

- Nenhum. Repositório fora do escopo (services/controllers) não alterado.
