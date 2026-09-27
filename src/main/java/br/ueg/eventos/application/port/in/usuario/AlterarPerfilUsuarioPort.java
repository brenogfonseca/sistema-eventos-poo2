package br.ueg.eventos.application.port.in.usuario;

import br.ueg.eventos.domain.model.Perfil;
import br.ueg.eventos.domain.model.Usuario;

/**
 * Porta de entrada: caso de uso para alterar o perfil de um usuário.
 * Acesso restrito exclusivamente a administradores.
 */
public interface AlterarPerfilUsuarioPort {

    /**
     * Comando com o id do executor (deve ser ADMINISTRADOR),
     * o id do usuário alvo e o novo perfil a ser atribuído.
     */
    class ComandoAlterarPerfil {
        public final Integer idExecutor;
        public final Integer idAlvo;
        public final Perfil novoPerfil;

        public ComandoAlterarPerfil(Integer idExecutor, Integer idAlvo, Perfil novoPerfil) {
            this.idExecutor = idExecutor;
            this.idAlvo = idAlvo;
            this.novoPerfil = novoPerfil;
        }
    }

    /**
     * Altera o perfil do usuário alvo. A validação de permissão é delegada
     * ao próprio domínio via {@code Usuario.alterarPerfil(executor, novoPerfil)}.
     *
     * @param comando dados da alteração de perfil
     * @return o usuário com o perfil atualizado
     */
    Usuario executar(ComandoAlterarPerfil comando);
}
