package br.com.encomendas.model;

import br.com.encomendas.model.enums.StatusPedido;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StatusPedidoTest {

    @Test
    void recebidoPodeIrParaAguardandoMaterialProducaoECancelado() {
        assertTrue(StatusPedido.RECEBIDO.podeIrPara(StatusPedido.AGUARDANDO_MATERIAL));
        assertTrue(StatusPedido.RECEBIDO.podeIrPara(StatusPedido.EM_PRODUCAO));
        assertTrue(StatusPedido.RECEBIDO.podeIrPara(StatusPedido.CANCELADO));
        assertFalse(StatusPedido.RECEBIDO.podeIrPara(StatusPedido.PRONTO));
        assertFalse(StatusPedido.RECEBIDO.podeIrPara(StatusPedido.ENTREGUE));
    }

    @Test
    void aguardandoMaterialPodeIrParaProducaoECancelado() {
        assertTrue(StatusPedido.AGUARDANDO_MATERIAL.podeIrPara(StatusPedido.EM_PRODUCAO));
        assertTrue(StatusPedido.AGUARDANDO_MATERIAL.podeIrPara(StatusPedido.CANCELADO));
        assertFalse(StatusPedido.AGUARDANDO_MATERIAL.podeIrPara(StatusPedido.PRONTO));
    }

    @Test
    void emProducaoPodeIrParaProntoECancelado() {
        assertTrue(StatusPedido.EM_PRODUCAO.podeIrPara(StatusPedido.PRONTO));
        assertTrue(StatusPedido.EM_PRODUCAO.podeIrPara(StatusPedido.CANCELADO));
        assertFalse(StatusPedido.EM_PRODUCAO.podeIrPara(StatusPedido.ENTREGUE));
    }

    @Test
    void prontoPodeIrParaEntregueECancelado() {
        assertTrue(StatusPedido.PRONTO.podeIrPara(StatusPedido.ENTREGUE));
        assertTrue(StatusPedido.PRONTO.podeIrPara(StatusPedido.CANCELADO));
        assertFalse(StatusPedido.PRONTO.podeIrPara(StatusPedido.EM_PRODUCAO));
    }

    @Test
    void entregueECanceladoSaoEstadosFinais() {
        for (StatusPedido destino : StatusPedido.values()) {
            assertFalse(StatusPedido.ENTREGUE.podeIrPara(destino));
            assertFalse(StatusPedido.CANCELADO.podeIrPara(destino));
        }
    }

    @Test
    void transicaoParaMesmoEstadoONullaEhInvalida() {
        assertFalse(StatusPedido.RECEBIDO.podeIrPara(StatusPedido.RECEBIDO));
        assertFalse(StatusPedido.RECEBIDO.podeIrPara(null));
    }
}
