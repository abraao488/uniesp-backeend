package br.com.encomendas.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ClienteTest {

    private static Cliente novo() {
        return ModelFixtures.cliente();
    }

    @Test
    void atualizarContatoSubstituiTelefoneEEmail() {
        Cliente c = novo();
        c.atualizarContato("11888887777", "novo@example.com");

        assertEquals("11888887777", c.getTelefone());
        assertEquals("novo@example.com", c.getEmail());
    }

    @Test
    void atualizarContatoRejeitaTelefoneNuloOuVazio() {
        Cliente c = novo();

        assertThrows(RegraDeNegocioException.class, () -> c.atualizarContato(null, "novo@example.com"));
        assertThrows(RegraDeNegocioException.class, () -> c.atualizarContato("   ", "novo@example.com"));

        assertEquals("11999990000", c.getTelefone());
    }

    @Test
    void atualizarContatoRejeitaEmailNuloOuInvalido() {
        Cliente c = novo();

        assertThrows(RegraDeNegocioException.class, () -> c.atualizarContato("11999990000", null));
        assertThrows(RegraDeNegocioException.class, () -> c.atualizarContato("11999990000", "sem-arroba"));

        assertEquals("maria@example.com", c.getEmail());
    }

    @Test
    void renomearSubstituiNomeComTrim() {
        Cliente c = novo();

        c.renomear("  Maria A. Silva ");

        assertEquals("Maria A. Silva", c.getNome());
    }

    @Test
    void renomearRejeitaNomeNuloOuVazio() {
        Cliente c = novo();

        assertThrows(RegraDeNegocioException.class, () -> c.renomear(null));
        assertThrows(RegraDeNegocioException.class, () -> c.renomear("   "));

        assertEquals("Maria Silva", c.getNome());
    }
}
