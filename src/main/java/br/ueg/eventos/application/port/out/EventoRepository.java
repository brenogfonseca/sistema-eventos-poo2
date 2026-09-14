package br.ueg.eventos.application.port.out;

import br.ueg.eventos.domain.model.Evento;
import java.util.List;
import java.util.Optional;

public interface EventoRepository {
    Evento salvar(Evento evento);
    Optional<Evento> buscarPorId(Long id);
    List<Evento> listarTodos();
}
