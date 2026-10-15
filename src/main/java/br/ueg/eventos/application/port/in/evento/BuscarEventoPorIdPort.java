package br.ueg.eventos.application.port.in.evento;

import br.ueg.eventos.domain.model.Evento;

import java.util.Optional;

/**
 * Porta de entrada: caso de uso para buscar um evento por ID.
 */
public interface BuscarEventoPorIdPort {

    /**
     * Busca um evento pelo seu identificador único.
     *
     * @param id identificador do evento
     * @return Optional com o evento se existir
     */
    Optional<Evento> executar(Long id);
}
