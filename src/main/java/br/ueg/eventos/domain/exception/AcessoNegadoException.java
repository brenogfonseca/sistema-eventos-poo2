package br.ueg.eventos.domain.exception;

/** Indica que o usuário autenticado não tem permissão para executar a ação. */
public class AcessoNegadoException extends RuntimeException {

    public AcessoNegadoException(String mensagem) {
        super(mensagem);
    }
}
