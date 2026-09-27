package br.ueg.eventos.application.port.out;

import br.ueg.eventos.domain.model.Usuario;

import java.util.List;
import java.util.Optional;

/**
 * Porta de saída: define o contrato de persistência para a entidade Usuario.
 * Qualquer mecanismo de armazenamento (in-memory, banco de dados, etc.) deve
 * implementar esta interface.
 */
public interface UsuarioRepository {

    /**
     * Salva um usuário. Se o id for nulo, cria um novo registro; caso
     * contrário, atualiza o existente.
     *
     * @param usuario o usuário a ser persistido
     * @return o usuário salvo (possivelmente com id gerado)
     */
    Usuario salvar(Usuario usuario);

    /**
     * Busca um usuário pelo seu identificador único.
     *
     * @param id o identificador do usuário
     * @return um Optional contendo o usuário, ou vazio se não encontrado
     */
    Optional<Usuario> buscarPorId(Integer id);

    /**
     * Busca um usuário pelo seu e-mail.
     *
     * @param email o e-mail do usuário
     * @return um Optional contendo o usuário, ou vazio se não encontrado
     */
    Optional<Usuario> buscarPorEmail(String email);

    /**
     * Lista todos os usuários cadastrados.
     *
     * @return lista de todos os usuários
     */
    List<Usuario> listarTodos();
}
