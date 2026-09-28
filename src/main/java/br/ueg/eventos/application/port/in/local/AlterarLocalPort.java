package br.ueg.eventos.application.port.in.local;

import br.ueg.eventos.domain.model.Local;

/**
 * Porta de entrada: caso de uso para alterar um local existente.
 */
public interface AlterarLocalPort {

    /**
     * Comando com os dados para alteração, incluindo o ID.
     */
    class ComandoAlterarLocal {
        public final Long id;
        public final String nome;

        public ComandoAlterarLocal(Long id, String nome) {
            this.id = id;
            this.nome = nome;
        }
    }

    /**
     * Executa o caso de uso de alteração de local.
     *
     * @param comando os novos dados e ID do local a ser alterado
     * @return o local alterado
     */
    Local executar(ComandoAlterarLocal comando);
}
