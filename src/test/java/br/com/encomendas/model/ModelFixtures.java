package br.com.encomendas.model;

import br.com.encomendas.model.enums.CanalOrigem;
import br.com.encomendas.model.enums.FormaPagamento;
import br.com.encomendas.model.enums.PerfilFuncionario;
import br.com.encomendas.model.enums.TipoPagamento;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Fabricas compartilhadas pelos testes de unidade do pacote model.
 */
final class ModelFixtures {

    private ModelFixtures() {
    }

    static Cliente cliente() {
        return new Cliente("Maria Silva", "11999990000", "maria@example.com",
                CanalOrigem.WHATSAPP, LocalDateTime.now());
    }

    static Funcionario atendente() {
        return new Funcionario("Joao Atendente", "joao@example.com",
                PerfilFuncionario.ATENDIMENTO, true);
    }

    static DiaProducao dia(int capacidade) {
        return new DiaProducao(LocalDate.now().plusDays(2), capacidade);
    }

    static Pedido pedido(DiaProducao dia) {
        return new Pedido(cliente(), atendente(), LocalDate.now().plusDays(7), dia);
    }

    static Produto produto() {
        return new Produto("Camiseta Personalizada", "Camiseta em algodao",
                new BigDecimal("80.00"), true);
    }

    static Pagamento pagamento(Pedido pedido, String valor) {
        return new Pagamento(pedido, new BigDecimal(valor), TipoPagamento.SINAL,
                FormaPagamento.PIX, LocalDateTime.now(), atendente());
    }
}
