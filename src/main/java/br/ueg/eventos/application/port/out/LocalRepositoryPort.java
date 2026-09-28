package br.ueg.eventos.application.port.out;

import br.ueg.eventos.domain.model.Local;

import java.util.List;
import java.util.Optional;

/**
 * Porta de saída: define o contrato de persistência para a entidade Local.
 * Qualquer mecanismo de armazenamento (em memória, banco de dados, etc.) deve implementar esta interface.
 */
public interface LocalRepositoryPort {

    /**
     * Salva um local. Se o id for nulo, cria um novo registro; caso contrário, atualiza o existente.
     *
     * @param local o local a ser persistido
     * @return o local salvo (possivelmente com id gerado)
     */
    Local salvar(Local local);

    /**
     * Busca um local pelo seu identificador único.
     *
     * @param id o identificador do local
     * @return um Optional contendo o local, ou vazio se não encontrado
     */
    Optional<Local> buscarPorId(Long id);

    /**
     * Lista todos os locais cadastrados.
     *
     * @return lista de todos os locais
     */
    List<Local> listarTodos();

    /**
     * Remove permanentemente um local pelo seu identificador.
     *
     * @param id o identificador do local a ser removido
     */
    void deletar(Long id);
}
