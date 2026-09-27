package br.ueg.eventos.application.port.in.usuario;

import br.ueg.eventos.domain.model.Perfil;
import br.ueg.eventos.domain.model.Usuario;

/**
 * Porta de entrada: caso de uso para cadastrar um novo usuário no sistema.
 */
public interface CadastrarUsuarioPort {

    /**
     * Comando imutável com os dados necessários para cadastrar um usuário.
     * Segue o padrão Command Object para manter a interface estável e explícita.
     */
    class ComandoCadastrarUsuario {
        public final String nome;
        public final String email;
        public final String senhaLimpa;
        public final Perfil perfil;

        public ComandoCadastrarUsuario(String nome, String email, String senhaLimpa, Perfil perfil) {
            this.nome = nome;
            this.email = email;
            this.senhaLimpa = senhaLimpa;
            this.perfil = perfil;
        }
    }

    /**
     * Executa o caso de uso de cadastro de usuário.
     *
     * @param comando os dados do novo usuário
     * @return o usuário cadastrado com id gerado
     */
    Usuario executar(ComandoCadastrarUsuario comando);
}
