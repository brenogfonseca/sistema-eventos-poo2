package br.ueg.eventos.application.port.in.evento;

import br.ueg.eventos.domain.model.Usuario;

/**
 * Porta de entrada: caso de uso para exclusão de um evento.
 * Exige a identificação do usuário executor para validação de segurança.
 */
public interface ExcluirEventoPort {

    class ComandoExcluirEvento {
        public final Long idEvento;
        public final Usuario usuarioExecutor; // Usuário executor da operação

        public ComandoExcluirEvento(Long idEvento, Usuario usuarioExecutor) {
            this.idEvento = idEvento;
            this.usuarioExecutor = usuarioExecutor;
        }
    }

    /**
     * Executa a exclusão do evento após validação de permissões de segurança.
     *
     * @param comando identificação do evento e do executor
     */
    void executar(ComandoExcluirEvento comando);
}
