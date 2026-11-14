package br.com.encomendas.model;

import br.com.encomendas.model.enums.OrigemAprovacao;
import br.com.encomendas.model.enums.StatusPedido;
import br.com.encomendas.model.enums.StatusVersao;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PedidoTest {

    private static DiaProducao dia(int capacidade) {
        return ModelFixtures.dia(capacidade);
    }

    private static Pedido pedido(DiaProducao dia) {
        return ModelFixtures.pedido(dia);
    }

    private static VersaoPedido versaoAprovada(Pedido p) {
        VersaoPedido versao = p.criarVersao(1, "bordado frontal", ModelFixtures.atendente());
        versao.adicionarItem(ModelFixtures.produto(), 2, new BigDecimal("80.00"), "azul");
        p.aprovarVersao(1, ModelFixtures.atendente(), OrigemAprovacao.ATENDENTE);
        return versao;
    }

    private static void pagamentoTotal(Pedido p) {
        p.registrarPagamento(ModelFixtures.pagamento(p, p.getValorTotal().toPlainString()));
    }

    private static Pedido pronto(DiaProducao d) {
        Pedido p = pedido(d);
        versaoAprovada(p);
        p.marcarAguardandoMaterial();
        p.iniciarProducao();
        p.marcarPronto();
        return p;
    }

    // ---------- criarVersao ----------

    @Test
    void criarVersaoCriaVersaoPendente() {
        Pedido p = pedido(dia(3));

        VersaoPedido v = p.criarVersao(1, "bordado frontal", ModelFixtures.atendente());

        assertNotNull(v);
        assertEquals(1, p.getVersoes().size());
        assertEquals(v, p.getVersoes().get(0));
    }

    @Test
    void criarVersaoRejeitaNumeroInvalidoEDuplicado() {
        Pedido p = pedido(dia(3));
        p.criarVersao(1, "a", ModelFixtures.atendente());

        assertThrows(RegraDeNegocioException.class, () -> p.criarVersao(0, "x", ModelFixtures.atendente()));
        assertThrows(RegraDeNegocioException.class, () -> p.criarVersao(-1, "x", ModelFixtures.atendente()));
        assertThrows(RegraDeNegocioException.class, () -> p.criarVersao(1, "x", ModelFixtures.atendente()));
    }

    @Test
    void criarVersaoRejeitaCriadorNuloOuInativo() {
        Pedido p = pedido(dia(3));
        Funcionario inativo = ModelFixtures.atendente();
        inativo.desativar();

        assertThrows(RegraDeNegocioException.class, () -> p.criarVersao(1, "x", null));
        assertThrows(RegraDeNegocioException.class, () -> p.criarVersao(1, "x", inativo));
        assertEquals(0, p.getVersoes().size());
    }

    @Test
    void criarVersaoRejeitaPedidoEntregueOuCancelado() {
        Pedido entregue = pronto(dia(3));
        pagamentoTotal(entregue);
        entregue.registrarEntrega();

        Pedido cancelado = pedido(dia(3));
        cancelado.cancelar();

        assertThrows(RegraDeNegocioException.class,
                () -> entregue.criarVersao(1, "x", ModelFixtures.atendente()));
        assertThrows(RegraDeNegocioException.class,
                () -> cancelado.criarVersao(1, "x", ModelFixtures.atendente()));
    }

    // ---------- aprovarVersaoPeloCliente / aprovarVersaoInternamente ----------

    @Test
    void aprovarVersaoPeloClienteAprovaComOrigemClienteEVazaAnteriores() {
        Pedido p = pedido(dia(3));
        VersaoPedido v1 = p.criarVersao(1, "a", ModelFixtures.atendente());
        p.aprovarVersaoPeloCliente(1);
        assertEquals(OrigemAprovacao.CLIENTE_VIA_LINK, v1.getOrigemAprovacao());
        assertTrue(v1.isAprovada());

        p.criarVersao(2, "b", ModelFixtures.atendente());
        p.aprovarVersaoPeloCliente(2);

        assertFalse(v1.isAprovada());
        assertEquals("INVALIDADA", v1.getStatus().name());
        assertEquals(p.getVersaoVigente().getNumero(), 2);
    }

    @Test
    void aprovarVersaoInternamenteAprovaComOrigemAtendente() {
        Pedido p = pedido(dia(3));
        VersaoPedido v1 = p.criarVersao(1, "a", ModelFixtures.atendente());

        p.aprovarVersaoInternamente(1, ModelFixtures.atendente());

        assertEquals(OrigemAprovacao.ATENDENTE, v1.getOrigemAprovacao());
        assertTrue(v1.isAprovada());
        assertNotNull(v1.getAprovadaEm());
        assertEquals(p.getVersaoVigente(), v1);
    }

    @Test
    void aprovarVersaoRejeitaNumeroInexistenteEDuplaAprovacao() {
        Pedido p = pedido(dia(3));
        VersaoPedido v1 = p.criarVersao(1, "a", ModelFixtures.atendente());

        assertThrows(RegraDeNegocioException.class, () -> p.aprovarVersaoPeloCliente(99));
        p.aprovarVersaoInternamente(1, ModelFixtures.atendente());
        assertThrows(RegraDeNegocioException.class, () -> p.aprovarVersaoPeloCliente(1));
        assertTrue(v1.isAprovada());
    }

    // ---------- registrarPagamento ----------

    @Test
    void registrarPagamentoSomaTotalPago() {
        Pedido p = pedido(dia(3));
        versaoAprovada(p);

        p.registrarPagamento(ModelFixtures.pagamento(p, "50.00"));

        assertEquals(0, p.getTotalPago().compareTo(new BigDecimal("50.00")));
        assertEquals(1, p.getPagamentos().size());
    }

    @Test
    void registrarPagamentoRejeitaNuloEPedidoCancelado() {
        Pedido p = pedido(dia(3));
        versaoAprovada(p);

        assertThrows(RegraDeNegocioException.class, () -> p.registrarPagamento(null));

        p.cancelar();
        assertThrows(RegraDeNegocioException.class,
                () -> p.registrarPagamento(ModelFixtures.pagamento(p, "10.00")));
        assertEquals(0, p.getPagamentos().size());
    }

    // ---------- agendarEntrega ----------

    @Test
    void agendarEntregaSubstituiDataEntrega() {
        Pedido p = pedido(dia(3));
        LocalDate nova = LocalDate.now().plusDays(30);

        p.agendarEntrega(nova);

        assertEquals(nova, p.getDataEntrega());
    }

    @Test
    void agendarEntregaRejeitaDataNula() {
        Pedido p = pedido(dia(3));

        assertThrows(RegraDeNegocioException.class, () -> p.agendarEntrega((LocalDate) null));
    }

    @Test
    void agendarEntregaComNovoDiaTrocaDiaERealocaVaga() {
        DiaProducao d1 = dia(1);
        DiaProducao d2 = dia(3);
        Pedido p = pedido(d1);
        assertEquals(1, d1.getVagasOcupadas());

        p.agendarEntrega(LocalDate.now().plusDays(10), d2);

        assertEquals(0, d1.getVagasOcupadas());
        assertEquals(1, d2.getVagasOcupadas());
        assertEquals(d2, p.getDiaProducao());
    }

    @Test
    void agendarEntregaComDiaLotadoNaoAlteraEstado() {
        DiaProducao d1 = dia(1);
        DiaProducao d2 = dia(1);
        new Pedido(ModelFixtures.cliente(), ModelFixtures.atendente(),
                LocalDate.now().plusDays(7), d2);
        Pedido p = pedido(d1);
        LocalDate atual = p.getDataEntrega();

        assertThrows(RegraDeNegocioException.class,
                () -> p.agendarEntrega(LocalDate.now().plusDays(15), d2));

        assertEquals(d1, p.getDiaProducao());
        assertEquals(1, d1.getVagasOcupadas());
        assertEquals(1, d2.getVagasOcupadas());
        assertEquals(atual, p.getDataEntrega());
    }

    // ---------- marcarAguardandoMaterial ----------

    @Test
    void marcarAguardandoMaterialAposRecebido() {
        Pedido p = pedido(dia(3));

        p.marcarAguardandoMaterial();

        assertEquals(StatusPedido.AGUARDANDO_MATERIAL, p.getStatus());
    }

    @Test
    void marcarAguardandoMaterialInvalidoEmOutrosStatus() {
        for (StatusPedido status : StatusPedido.values()) {
            if (status == StatusPedido.RECEBIDO) {
                continue;
            }
            assertThrows(RegraDeNegocioException.class,
                    () -> construirEm(status).marcarAguardandoMaterial(),
                    "de " + status);
        }
    }

    // ---------- iniciarProducao ----------

    @Test
    void iniciarProducaoAposRecebidoEAguardandoMaterial() {
        Pedido p = pedido(dia(3));
        versaoAprovada(p);
        p.iniciarProducao();
        assertEquals(StatusPedido.EM_PRODUCAO, p.getStatus());

        Pedido p2 = pedido(dia(3));
        versaoAprovada(p2);
        p2.marcarAguardandoMaterial();
        p2.iniciarProducao();
        assertEquals(StatusPedido.EM_PRODUCAO, p2.getStatus());
    }

    @Test
    void iniciarProducaoInvalidoEmOutrosStatus() {
        assertThrows(RegraDeNegocioException.class, () -> construirEm(StatusPedido.PRONTO).iniciarProducao());
        assertThrows(RegraDeNegocioException.class, () -> construirEm(StatusPedido.CANCELADO).iniciarProducao());
        assertThrows(RegraDeNegocioException.class, () -> construirEm(StatusPedido.ENTREGUE).iniciarProducao());
    }

    @Test
    void iniciarProducaoRejeitaSemVersaoAprovada() {
        Pedido p = pedido(dia(3));
        assertThrows(RegraDeNegocioException.class, p::iniciarProducao);
        assertEquals(StatusPedido.RECEBIDO, p.getStatus());
    }

    @Test
    void pedidoRejeitaDiaProducaoNulo() {
        assertThrows(RegraDeNegocioException.class,
                () -> new Pedido(ModelFixtures.cliente(), ModelFixtures.atendente(),
                        LocalDate.now().plusDays(7), null));
    }

    // ---------- marcarPronto ----------

    @Test
    void marcarProntoAposProducaoComVersaoAprovada() {
        Pedido p = pronto(dia(3));

        assertEquals(StatusPedido.PRONTO, p.getStatus());
    }

    @Test
    void marcarProntoRejeitaForaDeProducao() {
        assertThrows(RegraDeNegocioException.class, () -> {
            Pedido p = pedido(dia(3));
            versaoAprovada(p);
            p.marcarPronto();
        });
        assertThrows(RegraDeNegocioException.class, () -> construirEm(StatusPedido.PRONTO).marcarPronto());
    }

    // ---------- registrarEntrega ----------

    @Test
    void registrarEntregaEntregueELiberaVaga() {
        DiaProducao d = dia(3);
        Pedido p = pronto(d);
        pagamentoTotal(p);

        p.registrarEntrega();

        assertEquals(StatusPedido.ENTREGUE, p.getStatus());
        assertEquals(0, d.getVagasOcupadas());
    }

    @Test
    void registrarEntregaRejeitaComSaldoDevedor() {
        DiaProducao d = dia(3);
        Pedido p = pronto(d);
        p.registrarPagamento(ModelFixtures.pagamento(p, "10.00"));

        assertThrows(RegraDeNegocioException.class, p::registrarEntrega);

        assertEquals(StatusPedido.PRONTO, p.getStatus());
        assertEquals(1, d.getVagasOcupadas());
    }

    @Test
    void registrarEntregaRejeitaStatusInvalido() {
        assertThrows(RegraDeNegocioException.class, () -> pedido(dia(3)).registrarEntrega());
        assertThrows(RegraDeNegocioException.class, () -> construirEm(StatusPedido.RECEBIDO).registrarEntrega());
        assertThrows(RegraDeNegocioException.class, () -> construirEm(StatusPedido.EM_PRODUCAO).registrarEntrega());
    }

    // ---------- requisitos: versao vigente, dia cheio, entrega/saldo, pagamento excede, transicao ----------

    @Test
    void versaoNaoAprovadaNaoEhVigente() {
        Pedido p = pedido(dia(3));
        p.criarVersao(1, "a", ModelFixtures.atendente());
        assertNull(p.getVersaoVigente());
    }

    @Test
    void versaoVigenteAposAprovacaoENovaAprobacaoInvalidaAnterior() {
        Pedido p = pedido(dia(3));
        VersaoPedido v1 = p.criarVersao(1, "a", ModelFixtures.atendente());
        p.aprovarVersao(1, ModelFixtures.atendente(), OrigemAprovacao.ATENDENTE);
        assertEquals(v1, p.getVersaoVigente());
        assertTrue(v1.isAprovada());

        VersaoPedido v2 = p.criarVersao(2, "b", ModelFixtures.atendente());
        p.aprovarVersao(2, ModelFixtures.atendente(), OrigemAprovacao.ATENDENTE);

        assertFalse(v1.isAprovada());
        assertEquals(StatusVersao.INVALIDADA, v1.getStatus());
        assertTrue(v2.isAprovada());
        assertEquals(v2, p.getVersaoVigente());
    }

    @Test
    void diaProducaoLotadoRecusaNovoAgendamento() {
        DiaProducao d = dia(1);
        new Pedido(ModelFixtures.cliente(), ModelFixtures.atendente(),
                LocalDate.now().plusDays(7), d);
        assertEquals(1, d.getVagasOcupadas());

        assertThrows(RegraDeNegocioException.class,
                () -> new Pedido(ModelFixtures.cliente(), ModelFixtures.atendente(),
                        LocalDate.now().plusDays(7), d));
    }

    @Test
    void entregaBloqueadaComSaldoPendenteELiberadaAposPagamentoTotal() {
        DiaProducao d = dia(3);
        Pedido p = pronto(d);
        p.registrarPagamento(ModelFixtures.pagamento(p, "10.00"));
        assertFalse(p.isSaldoQuitado());

        assertThrows(RegraDeNegocioException.class, p::registrarEntrega);
        assertEquals(StatusPedido.PRONTO, p.getStatus());

        p.registrarPagamento(ModelFixtures.pagamento(p, p.getSaldoDevedor().toPlainString()));
        assertTrue(p.isSaldoQuitado());
        p.registrarEntrega();
        assertEquals(StatusPedido.ENTREGUE, p.getStatus());
    }

    @Test
    void pagamentoMaiorQueSaldoERecusado() {
        Pedido p = pedido(dia(3));
        versaoAprovada(p);
        BigDecimal saldo = p.getSaldoDevedor();
        assertTrue(saldo.compareTo(BigDecimal.ZERO) > 0);

        assertThrows(RegraDeNegocioException.class,
                () -> p.registrarPagamento(ModelFixtures.pagamento(p, saldo.add(BigDecimal.ONE).toPlainString())));
        assertEquals(0, p.getPagamentos().size());
        assertEquals(BigDecimal.ZERO, p.getTotalPago());
    }

    @Test
    void transicaoDeStatusInvalidaLancaExcecao() {
        Pedido p = pedido(dia(3));
        assertThrows(RegraDeNegocioException.class, () -> construirEm(StatusPedido.RECEBIDO).marcarPronto());
        assertThrows(RegraDeNegocioException.class, () -> construirEm(StatusPedido.RECEBIDO).entregar());

        Pedido entregue = pronto(dia(3));
        pagamentoTotal(entregue);
        entregue.registrarEntrega();
        assertThrows(RegraDeNegocioException.class, entregue::iniciarProducao);
        assertEquals(StatusPedido.ENTREGUE, entregue.getStatus());
    }

    // ---------- cancelar ----------

    @Test
    void cancelarAposRecebidoLiberaVaga() {
        DiaProducao d = dia(3);
        Pedido p = pedido(d);
        assertEquals(1, d.getVagasOcupadas());

        p.cancelar();

        assertEquals(StatusPedido.CANCELADO, p.getStatus());
        assertEquals(0, d.getVagasOcupadas());
    }

    @Test
    void cancelarRejeitaEntregueEJaCancelado() {
        DiaProducao d = dia(3);
        Pedido p = pronto(d);
        pagamentoTotal(p);
        p.registrarEntrega();
        assertThrows(RegraDeNegocioException.class, p::cancelar);

        Pedido c = pedido(d);
        c.cancelar();
        assertThrows(RegraDeNegocioException.class, c::cancelar);
        assertEquals(0, d.getVagasOcupadas());
    }

    // ---------- auxiliares ----------

    private static Pedido construirEm(StatusPedido status) {
        Pedido p = pedido(dia(3));
        switch (status) {
            case AGUARDANDO_MATERIAL:
                p.marcarAguardandoMaterial();
                break;
            case EM_PRODUCAO:
                p.iniciarProducao();
                break;
            case PRONTO:
                versaoAprovada(p);
                p.iniciarProducao();
                p.marcarPronto();
                break;
            case CANCELADO:
                p.cancelar();
                break;
            case RECEBIDO:
                break;
            case ENTREGUE:
                versaoAprovada(p);
                p.iniciarProducao();
                p.marcarPronto();
                pagamentoTotal(p);
                p.registrarEntrega();
                break;
            default:
                throw new IllegalArgumentException("status inesperado: " + status);
        }
        return p;
    }
}
