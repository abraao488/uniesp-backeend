package br.com.encomendas.model;

import br.com.encomendas.model.enums.OrigemAprovacao;
import br.com.encomendas.model.enums.StatusVersao;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VersaoPedidoTest {

    private static Pedido pedido() {
        return ModelFixtures.pedido(ModelFixtures.dia(3));
    }

    private static VersaoPedido versaoPendente() {
        return pedido().criarVersao(1, "bordado frontal", ModelFixtures.atendente());
    }

    @Test
    void adicionarItemRetornaEAdicionaItem() {
        VersaoPedido versao = versaoPendente();

        ItemPedido item = versao.adicionarItem(ModelFixtures.produto(), 2,
                new BigDecimal("80.00"), "azul");

        assertNotNull(item);
        assertEquals(1, versao.getItens().size());
        assertEquals(0, versao.getValorTotal().compareTo(new BigDecimal("160.00")));
    }

    @Test
    void adicionarItemRejeitaEmVersaoAprovada() {
        VersaoPedido versao = versaoPendente();
        versao.aprovar(null, OrigemAprovacao.CLIENTE_VIA_LINK);

        assertThrows(RegraDeNegocioException.class,
                () -> versao.adicionarItem(ModelFixtures.produto(), 1, new BigDecimal("80.00"), null));
        assertEquals(0, versao.getItens().size());
    }

    @Test
    void removerItemRemoveItemDaVersao() {
        VersaoPedido versao = versaoPendente();
        ItemPedido item = versao.adicionarItem(ModelFixtures.produto(), 1,
                new BigDecimal("80.00"), null);

        versao.removerItem(item);

        assertEquals(0, versao.getItens().size());
        assertEquals(0, versao.getValorTotal().compareTo(BigDecimal.ZERO));
    }

    @Test
    void removerItemRejeitaNuloForaDaVersaoEEmVersaoAprovada() {
        VersaoPedido versao = versaoPendente();
        ItemPedido item = versao.adicionarItem(ModelFixtures.produto(), 1,
                new BigDecimal("80.00"), null);
        ItemPedido deOutra = versaoPendente()
                .adicionarItem(ModelFixtures.produto(), 1, new BigDecimal("80.00"), null);

        assertThrows(RegraDeNegocioException.class, () -> versao.removerItem((ItemPedido) null));
        assertThrows(RegraDeNegocioException.class, () -> versao.removerItem(deOutra));
        assertThrows(RegraDeNegocioException.class, () -> versao.removerItem((Long) null));
        assertThrows(RegraDeNegocioException.class, () -> versao.removerItem(-5L));

        versao.aprovar(null, OrigemAprovacao.CLIENTE_VIA_LINK);
        assertThrows(RegraDeNegocioException.class, () -> versao.removerItem(item));
        assertEquals(1, versao.getItens().size());
    }

    @Test
    void alterarDescricaoSubstituiComTrim() {
        VersaoPedido versao = versaoPendente();

        versao.alterarDescricao("  bordado na mangas  ");

        assertEquals("bordado na mangas", versao.getDescricaoPersonalizacao());
    }

    @Test
    void alterarDescricaoNuloMantemAnteriorERejeitaEmAprovada() {
        VersaoPedido versao = versaoPendente();

        versao.alterarDescricao(null);
        assertEquals("bordado frontal", versao.getDescricaoPersonalizacao());

        versao.aprovar(null, OrigemAprovacao.CLIENTE_VIA_LINK);
        assertThrows(RegraDeNegocioException.class,
                () -> versao.alterarDescricao("nova"));
        assertEquals(StatusVersao.APROVADA, versao.getStatus());
        assertTrue(versao.isAprovada());
    }
}
