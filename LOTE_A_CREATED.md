# Lote A - Arquivos Criados

## Enums (7 arquivos)
1. `src/main/java/br/com/encomendas/model/CanalOrigem.java` - WHATSAPP, INSTAGRAM, PRESENCIAL
2. `src/main/java/br/com/encomendas/model/PerfilFuncionario.java` - ATENDIMENTO, PRODUCAO, ENTREGA, PROPRIETARIO
3. `src/main/java/br/com/encomendas/model/StatusPedido.java` - RECEBIDO, AGUARDANDO_MATERIAL, EM_PRODUCAO, PRONTO, ENTREGUE, CANCELADO
4. `src/main/java/br/com/encomendas/model/StatusVersao.java` - PENDENTE_APROVACAO, APROVADA, INVALIDADA
5. `src/main/java/br/com/encomendas/model/OrigemAprovacao.java` - CLIENTE_VIA_LINK, ATENDENTE
6. `src/main/java/br/com/encomendas/model/TipoPagamento.java` - SINAL, PARCIAL, RESTANTE
7. `src/main/java/br/com/encomendas/model/FormaPagamento.java` - PIX, CARTAO, DINHEIRO

## Exception (1 arquivo)
8. `src/main/java/br/com/encomendas/model/RegruDeNegocioException.java` - RuntimeException com construtor String

## Entidades (5 arquivos)
9. `src/main/java/br/com/encomendas/model/Cliente.java`
   - Construtor valida: nome, telefone, email obrigatorios e nao vazios; email contem @; canalOrigem, dataCadastro obrigatorios
   - Getters imutaveis

10. `src/main/java/br/com/encomendas/model/Funcionario.java`
    - Construtor valida: nome, email obrigatorios; email contem @; perfil obrigatorio
    - ativar()/desativar() idempotentes

11. `src/main/java/br/com/encomendas/model/Produto.java`
    - Construtor valida: nome obrigatorio; precoBase > 0
    - reajustarPreco() rejeita null, zero, negativo
    - ativar()/desativar()

12. `src/main/java/br/com/encomendas/model/DiaProducao.java`
    - Construtor valida: data obrigatoria; capacidade > 0
    - vagasOcupadas inicia em 0
    - reservarVaga() lanca exception se sem vaga
    - liberarVaga() nunca negativo
    - vagasDisponiveis(), temVaga()

13. `src/main/java/br/com/encomendas/model/LinkAcompanhamento.java`
    - token gerado com UUID.randomUUID().toString()
    - criadoEm = LocalDateTime.now()
    - pedido obrigatorio
   - desativar()/reativar()

## Verificacoes Contractuais
- Java 17: OK (sem Lombok, sem setters genericos, sem annotations de JPA)
- Construtores lancam RegraDeNegocioException: OK
- Sem pom.xml edicao: OK
- Sem arquivos fora de src/main/java/br/com/encomendas/model/: OK
- Sem Pedido, VersaoPedido, ItemPedido, Pagamento: OK (Lote B)