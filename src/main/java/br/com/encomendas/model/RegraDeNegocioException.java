package br.com.encomendas.model;

/**
 * Exceção lançada quando uma regra de negócio é violada.
 * É uma RuntimeException para não obrigar captura explícita em toda a pilha,
 * mas deve ser tratada na camada de controle para retornar HTTP 4xx apropriado.
 */
public class RegraDeNegocioException extends RuntimeException {

    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }

    public RegraDeNegocioException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}