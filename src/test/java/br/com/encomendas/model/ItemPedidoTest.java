package br.com.encomendas.model;

import br.com.encomendas.model.enums.OrigemAprovacao;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ItemPedidoTest {

    private static ItemPedido item() {
        VersaoPedido versao = ModelFixtures.pedido(ModelFixtures.dia(3))
                .criarVersao(1, "descricao", ModelFixtures.atendente());
        return versao.adicionarItem(ModelFixtures.produto(), 3, new BigDecimal("80.00"), "azul");
    }

    @Test
    void alterarQuantidadeAtualizaQuantidadeESubtotal() {
        ItemPedido item = item();

        item.alterarQuantidade(5);

        assertEquals(5, item.getQuantidade());
        assertEquals(0, item.getSubtotal().compareTo(new BigDecimal("400.00")));
    }

    @Test
    void alterarQuantidadeRejeitaZeroENegativo() {
        ItemPedido item = item();

        assertThrows(RegraDeNegocioException.class, () -> item.alterarQuantidade(0));
        assertThrows(RegraDeNegocioException.class, () -> item.alterarQuantidade(-2));

        assertEquals(3, item.getQuantidade());
    }

    @Test
    void alterarQuantidadeRejeitaEmVersaoAprovada() {
        ItemPedido item = item();
        item.getVersaoPedido().aprovar(null, OrigemAprovacao.CLIENTE_VIA_LINK);

        assertThrows(RegraDeNegocioException.class, () -> item.alterarQuantidade(2));

        assertEquals(3, item.getQuantidade());
    }
}
