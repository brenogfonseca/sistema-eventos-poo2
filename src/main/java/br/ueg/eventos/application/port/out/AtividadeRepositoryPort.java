package br.ueg.eventos.application.port.out;

import br.ueg.eventos.domain.model.Atividade;
import java.util.List;
import java.util.Optional;

public interface AtividadeRepositoryPort {

    Atividade salvar(Atividade atividade);

    Optional<Atividade> buscarPorId(Long id);

    List<Atividade> listarPorEvento(Long idEvento);

    /** Atividades no mesmo local; a camada de aplicação verifica a sobreposição. */
    default List<Atividade> listarPorLocal(Long idLocal) {
        return List.of();
    }

    void excluir(Long id);

    /** Atomically consumes one remaining spot when capacity is limited. */
    default boolean consumirVaga(Long id) {
        Atividade atividade = buscarPorId(id).orElseThrow();
        if (!atividade.isControlaVagas()) return true;
        atividade.registrarInscricao();
        salvar(atividade);
        return true;
    }

    /** Returns one spot after an active activity enrollment is cancelled. */
    default void liberarVaga(Long id) {
        Atividade atividade = buscarPorId(id).orElseThrow();
        atividade.cancelarInscricao();
        salvar(atividade);
    }
}
