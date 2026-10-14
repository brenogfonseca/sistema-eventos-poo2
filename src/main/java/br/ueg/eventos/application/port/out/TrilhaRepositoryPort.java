package br.ueg.eventos.application.port.out;

import br.ueg.eventos.domain.model.Trilha;

import java.util.List;
import java.util.Optional;

/** Define as operações de persistência de Trilha. */
public interface TrilhaRepositoryPort {

    // Salva ou atualiza uma trilha.
    Trilha salvar(Trilha trilha);

    // Busca uma trilha pelo ID.
    Optional<Trilha> buscarPorId(Long id);

    // Lista as trilhas cadastradas.
    List<Trilha> listarTodos();

    // Exclui uma trilha pelo ID.
    void deletar(Long id);
}