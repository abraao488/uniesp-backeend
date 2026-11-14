package br.com.encomendas.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DiaProducaoTest {

    private static DiaProducao dia(int capacidade) {
        return ModelFixtures.dia(capacidade);
    }

    @Test
    void vagasOcupadasParteDeZero() {
        assertEquals(0, dia(5).getVagasOcupadas());
    }

    @Test
    void vagasLivresRefleteCapacidadeMenosOcupadas() {
        DiaProducao d = dia(3);

        d.reservar();
        d.reservar();

        assertEquals(1, d.vagasLivres());
        assertEquals(2, d.getVagasOcupadas());
    }

    @Test
    void temVagaLivreMudaAoLotarEDesocupar() {
        DiaProducao d = dia(1);
        assertTrue(d.temVagaLivre());

        d.reservar();
        assertFalse(d.temVagaLivre());

        d.liberar();
        assertTrue(d.temVagaLivre());
    }

    @Test
    void reservarSemVagaLancaExcecao() {
        DiaProducao d = dia(1);
        d.reservar();

        assertThrows(RegraDeNegocioException.class, d::reservar);
        assertEquals(1, d.getVagasOcupadas());
    }

    @Test
    void liberarSemVagaOcupadaNaoFicaNegativo() {
        DiaProducao d = dia(1);

        d.liberar();

        assertEquals(0, d.getVagasOcupadas());
    }

    @Test
    void ajustarCapacidadeAtualizaERecalculaVagas() {
        DiaProducao d = dia(2);
        d.reservar();

        d.ajustarCapacidade(5);

        assertEquals(5, d.getCapacidade());
        assertEquals(4, d.vagasLivres());
    }

    @Test
    void ajustarCapacidadeRejeitaZeroNegativoEAbaixoDoOcupado() {
        DiaProducao d = dia(2);
        d.reservar();
        d.reservar();

        assertThrows(RegraDeNegocioException.class, () -> d.ajustarCapacidade(0));
        assertThrows(RegraDeNegocioException.class, () -> d.ajustarCapacidade(-2));
        assertThrows(RegraDeNegocioException.class, () -> d.ajustarCapacidade(1));

        assertEquals(2, d.getCapacidade());
    }

    @Test
    void reservarQuantidadeOcupaVariasVagas() {
        DiaProducao d = dia(5);

        d.reservar(3);

        assertEquals(3, d.getVagasOcupadas());
        assertEquals(2, d.vagasLivres());
    }

    @Test
    void reservarQuantidadeInsuficienteLancaExcecao() {
        DiaProducao d = dia(2);

        assertThrows(RegraDeNegocioException.class, () -> d.reservar(3));
        assertThrows(RegraDeNegocioException.class, () -> d.reservar(0));

        assertEquals(0, d.getVagasOcupadas());
    }

    @Test
    void liberarQuantidadeLiberaVariasVagas() {
        DiaProducao d = dia(5);
        d.reservar(4);

        d.liberar(2);

        assertEquals(2, d.getVagasOcupadas());
    }

    @Test
    void liberarQuantidadeMaiorQueOcupadaLancaExcecao() {
        DiaProducao d = dia(5);
        d.reservar(2);

        assertThrows(RegraDeNegocioException.class, () -> d.liberar(3));
        assertThrows(RegraDeNegocioException.class, () -> d.liberar(0));

        assertEquals(2, d.getVagasOcupadas());
    }
}
