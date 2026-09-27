package br.ueg.eventos.application.port.out;

import br.ueg.eventos.domain.model.Atividade;
import java.util.List;
import java.util.Optional;

public interface AtividadeRepositoryPort {

    Atividade salvar(Atividade atividade);

    Optional<Atividade> buscarPorId(Long id);

    List<Atividade> listarPorEvento(Long idEvento);

    void excluir(Long id);
}