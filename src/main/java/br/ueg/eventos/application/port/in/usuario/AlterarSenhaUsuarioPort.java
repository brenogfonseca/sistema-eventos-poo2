package br.ueg.eventos.application.port.in.usuario;

import br.ueg.eventos.domain.model.Usuario;

/**
 * Porta de entrada: caso de uso para alterar a senha de um usuário autenticado.
 */
public interface AlterarSenhaUsuarioPort {

    /**
     * Comando imutável com os dados necessários para alterar a senha.
     */
    class ComandoAlterarSenha {
        public final Integer idUsuario;
        public final String novaSenhaLimpa;

        public ComandoAlterarSenha(Integer idUsuario, String novaSenhaLimpa) {
            this.idUsuario = idUsuario;
            this.novaSenhaLimpa = novaSenhaLimpa;
        }
    }

    /**
     * Executa o caso de uso de alteração de senha.
     *
     * @param comando os dados necessários para a troca de senha
     * @return o usuário com a senha atualizada
     */
    Usuario executar(ComandoAlterarSenha comando);
}
