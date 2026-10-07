package br.ueg.eventos.application.service.evento;

import br.ueg.eventos.application.port.in.evento.ListarEventosPort;
import br.ueg.eventos.application.port.out.EventoRepositoryPort;
import br.ueg.eventos.domain.model.Evento;

import java.util.List;

public class ListarEventosService implements ListarEventosPort {

    private final EventoRepositoryPort eventoRepository;

    public ListarEventosService(EventoRepositoryPort eventoRepository) {
        this.eventoRepository = eventoRepository;
    }

    @Override
    public List<Evento> executar() {
        return eventoRepository.listarTodos().stream()
                .filter(evento -> !evento.isDeletado())
                .collect(java.util.stream.Collectors.toList());
    }

}
