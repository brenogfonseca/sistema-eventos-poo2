package br.ueg.eventos.application.port.in.usuario;

import br.ueg.eventos.domain.model.Usuario;

import java.util.List;

/**
 * Porta de entrada: caso de uso para listar todos os usuários cadastrados.
 * Acesso restrito a administradores.
 */
public interface ListarUsuariosPort {

    /**
     * Comando com o id do usuário executor (quem está fazendo a requisição).
     * O service validará se ele tem permissão de administrador.
     */
    class ComandoListarUsuarios {
        public final Integer idExecutor;

        public ComandoListarUsuarios(Integer idExecutor) {
            this.idExecutor = idExecutor;
        }
    }

    /**
     * Retorna todos os usuários. Apenas ADMINISTRADOR pode executar.
     *
     * @param comando identificação do executor
     * @return lista de todos os usuários
     */
    List<Usuario> executar(ComandoListarUsuarios comando);
}
