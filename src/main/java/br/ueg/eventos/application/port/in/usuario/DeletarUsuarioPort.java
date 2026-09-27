package br.ueg.eventos.application.port.in.usuario;

/**
 * Porta de entrada: caso de uso para deletar um usuário do sistema.
 * Acesso restrito exclusivamente a administradores.
 * Um administrador não pode deletar a si mesmo.
 */
public interface DeletarUsuarioPort {

    /**
     * Comando com o id do executor (deve ser ADMINISTRADOR)
     * e o id do usuário a ser removido.
     */
    class ComandoDeletarUsuario {
        public final Integer idExecutor;
        public final Integer idAlvo;

        public ComandoDeletarUsuario(Integer idExecutor, Integer idAlvo) {
            this.idExecutor = idExecutor;
            this.idAlvo = idAlvo;
        }
    }

    /**
     * Remove permanentemente o usuário alvo.
     *
     * @param comando identificação do executor e do alvo
     */
    void executar(ComandoDeletarUsuario comando);
}
