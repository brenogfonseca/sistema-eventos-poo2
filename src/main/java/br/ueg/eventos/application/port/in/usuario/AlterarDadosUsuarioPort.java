package br.ueg.eventos.application.port.in.usuario;

import br.ueg.eventos.domain.model.Usuario;

/**
 * Porta de entrada: caso de uso para alterar os dados básicos (nome e e-mail)
 * de um usuário. Acesso permitido ao próprio usuário ou a um administrador.
 */
public interface AlterarDadosUsuarioPort {

    /**
     * Comando com o id do executor, o id do usuário alvo e os novos dados.
     * Campos nulos ou em branco são ignorados (atualização parcial).
     */
    class ComandoAlterarDados {
        public final Integer idExecutor;
        public final Integer idAlvo;
        public final String novoNome;
        public final String novoEmail;

        public ComandoAlterarDados(Integer idExecutor, Integer idAlvo, String novoNome, String novoEmail) {
            this.idExecutor = idExecutor;
            this.idAlvo = idAlvo;
            this.novoNome = novoNome;
            this.novoEmail = novoEmail;
        }
    }

    /**
     * Altera nome e/ou e-mail do usuário alvo.
     * Apenas o próprio usuário ou um ADMINISTRADOR podem executar.
     *
     * @param comando dados da alteração
     * @return o usuário com os dados atualizados
     */
    Usuario executar(ComandoAlterarDados comando);
}
