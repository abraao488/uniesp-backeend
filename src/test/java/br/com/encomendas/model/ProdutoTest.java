package br.com.encomendas.model;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProdutoTest {

    private static Produto novo() {
        return ModelFixtures.produto();
    }

    @Test
    void alterarPrecoSubstituiPrecoBase() {
        Produto p = novo();

        p.alterarPreco(new BigDecimal("95.50"));

        assertEquals(0, p.getPrecoBase().compareTo(new BigDecimal("95.50")));
    }

    @Test
    void alterarPrecoRejeitaNuloZeroENegativo() {
        Produto p = novo();

        assertThrows(RegraDeNegocioException.class, () -> p.alterarPreco(null));
        assertThrows(RegraDeNegocioException.class, () -> p.alterarPreco(BigDecimal.ZERO));
        assertThrows(RegraDeNegocioException.class, () -> p.alterarPreco(new BigDecimal("-1.00")));

        assertEquals(0, p.getPrecoBase().compareTo(new BigDecimal("80.00")));
    }

    @Test
    void alterarDescricaoSubstituiComTrim() {
        Produto p = novo();

        p.alterarDescricao("  nova descricao  ");

        assertEquals("nova descricao", p.getDescricao());
    }

    @Test
    void alterarDescricaoNuloMantemValorAnterior() {
        Produto p = novo();

        p.alterarDescricao(null);

        assertEquals("Camiseta em algodao", p.getDescricao());
    }

    @Test
    void desativarEIdempotente() {
        Produto p = novo();

        p.desativar();
        p.desativar();

        assertFalse(p.isAtivo());
    }

    @Test
    void reativarRestauraProdutoAtivo() {
        Produto p = novo();
        p.desativar();

        p.reativar();

        assertTrue(p.isAtivo());
    }
}
