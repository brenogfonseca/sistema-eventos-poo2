package br.ueg.eventos.application.service.evento;

import br.ueg.eventos.application.port.in.evento.ListarEventosPort;
import br.ueg.eventos.application.port.out.EventoRepository;
import br.ueg.eventos.domain.model.Evento;

import java.util.List;

public class ListarEventosService implements ListarEventosPort {

    private final EventoRepository eventoRepository;

    public ListarEventosService(EventoRepository eventoRepository) {
        this.eventoRepository = eventoRepository;
    }

    @Override
    public List<Evento> executar() {
        return eventoRepository.listarTodos();
    }
}
