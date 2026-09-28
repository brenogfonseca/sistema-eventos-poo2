package br.ueg.eventos.application.port.in.local;

/**
 * Porta de entrada: caso de uso para deletar um local pelo seu identificador.
 */
public interface DeletarLocalPort {

    /**
     * Executa a remoção do local.
     *
     * @param id identificador do local a ser removido
     */
    void executar(Long id);
}
