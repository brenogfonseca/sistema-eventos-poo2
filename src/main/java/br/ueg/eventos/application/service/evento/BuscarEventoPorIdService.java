package br.ueg.eventos.application.service.evento;

import br.ueg.eventos.application.port.in.evento.BuscarEventoPorIdPort;
import br.ueg.eventos.application.port.out.EventoRepositoryPort;
import br.ueg.eventos.domain.model.Evento;

import java.util.Optional;

/**
 * Serviço de aplicação para buscar um evento por ID.
 */
public class BuscarEventoPorIdService implements BuscarEventoPorIdPort {

    private final EventoRepositoryPort eventoRepository;

    public BuscarEventoPorIdService(EventoRepositoryPort eventoRepository) {
        this.eventoRepository = eventoRepository;
    }

    @Override
    public Optional<Evento> executar(Long id) {
        if (id == null || id <= 0) {
            return Optional.empty();
        }
        return eventoRepository.buscarPorId(id).filter(e -> !e.isDeletado());
    }
}
