package br.ueg.eventos.application.port.out;

import br.ueg.eventos.domain.model.Tipo;

import java.util.List;
import java.util.Optional;

/** Define as operações de persistência de Tipo. */
public interface TipoRepositoryPort {

    // Salva ou atualiza um tipo.
    Tipo salvar(Tipo tipo);

    // Busca um tipo pelo ID.
    Optional<Tipo> buscarPorId(Long id);

    // Lista os tipos cadastrados.
    List<Tipo> listarTodos();

    // Exclui um tipo pelo ID.
    void deletar(Long id);
}