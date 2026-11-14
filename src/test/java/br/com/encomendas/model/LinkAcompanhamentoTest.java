package br.com.encomendas.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LinkAcompanhamentoTest {

    private static LinkAcompanhamento link() {
        return new LinkAcompanhamento(ModelFixtures.pedido(ModelFixtures.dia(3)));
    }

    @Test
    void tokenEhGeradoNaCriacao() {
        LinkAcompanhamento link = link();

        assertNotNull(link.getToken());
        assertEquals(36, link.getToken().length());
    }

    @Test
    void desativarTornaLinkInativo() {
        LinkAcompanhamento link = link();

        link.desativar();

        assertFalse(link.isAtivo());
    }

    @Test
    void reativarRestauraLinkAtivo() {
        LinkAcompanhamento link = link();
        link.desativar();

        link.reativar();

        assertTrue(link.isAtivo());
    }

    @Test
    void regenerarTokenTrocaTokenMantendoLink() {
        LinkAcompanhamento link = link();
        String tokenAnterior = link.getToken();

        link.regenerarToken();

        assertNotEquals(tokenAnterior, link.getToken());
        assertEquals(36, link.getToken().length());
        assertTrue(link.isAtivo());
    }
}
