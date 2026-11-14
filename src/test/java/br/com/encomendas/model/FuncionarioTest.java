package br.com.encomendas.model;

import br.com.encomendas.model.enums.PerfilFuncionario;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FuncionarioTest {

    private static Funcionario comPerfil(PerfilFuncionario perfil) {
        return new Funcionario("Joao", "joao@example.com", perfil, true);
    }

    @Test
    void alterarPerfilSubstituiPerfil() {
        Funcionario f = comPerfil(PerfilFuncionario.ATENDIMENTO);

        f.alterarPerfil(PerfilFuncionario.PRODUCAO);

        assertEquals(PerfilFuncionario.PRODUCAO, f.getPerfil());
    }

    @Test
    void alterarPerfilRejeitaPerfilNulo() {
        Funcionario f = comPerfil(PerfilFuncionario.ATENDIMENTO);

        assertThrows(RegraDeNegocioException.class, () -> f.alterarPerfil(null));

        assertEquals(PerfilFuncionario.ATENDIMENTO, f.getPerfil());
    }

    @Test
    void desativarTornaInativoEIdempotente() {
        Funcionario f = comPerfil(PerfilFuncionario.ATENDIMENTO);

        f.desativar();
        f.desativar();

        assertFalse(f.isAtivo());
    }

    @Test
    void reativarTornaAtivoEIdempotente() {
        Funcionario f = comPerfil(PerfilFuncionario.ATENDIMENTO);
        f.desativar();

        f.reativar();
        f.reativar();

        assertTrue(f.isAtivo());
    }

    @Test
    void podeAtenderApenasQuandoAtivoComPerfilAtendimento() {
        assertTrue(comPerfil(PerfilFuncionario.ATENDIMENTO).podeAtender());

        Funcionario inativo = comPerfil(PerfilFuncionario.ATENDIMENTO);
        inativo.desativar();
        assertFalse(inativo.podeAtender());

        assertFalse(comPerfil(PerfilFuncionario.PRODUCAO).podeAtender());
        assertFalse(comPerfil(PerfilFuncionario.ENTREGA).podeAtender());
    }

    @Test
    void podeEntregarApenasQuandoAtivoComPerfilEntrega() {
        assertTrue(comPerfil(PerfilFuncionario.ENTREGA).podeEntregar());

        Funcionario inativo = comPerfil(PerfilFuncionario.ENTREGA);
        inativo.desativar();
        assertFalse(inativo.podeEntregar());

        assertFalse(comPerfil(PerfilFuncionario.ATENDIMENTO).podeEntregar());
        assertTrue(comPerfil(PerfilFuncionario.PROPRIETARIO).podeEntregar());
    }
}
