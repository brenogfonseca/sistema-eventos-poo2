package br.ueg.eventos.domain.exception;

/**
 * Exceção base para violações de regras de negócio do domínio.
 */
public class RegraNegocioException extends RuntimeException {
    
    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
