package br.ueg.eventos.infrastructure.adapter.out.persistence;

import br.ueg.eventos.application.port.out.EventoRepository;
import br.ueg.eventos.domain.model.Evento;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryEventoRepository implements EventoRepository {

    private final Map<Long, Evento> eventos = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public Evento salvar(Evento evento) {
        if (evento.getId() == null) {
            Long novoId = idGenerator.getAndIncrement();
            
            Evento eventoSalvo = new Evento(novoId, evento.getTitulo(), evento.getDescricao(), evento.getPeriodo(),
                    evento.getCapacidadeMaxima(), evento.isInscricoesAbertas(), evento.isDeletado(),
                    evento.isCertificado(), evento.getFrequenciaMinima());
            
            eventos.put(novoId, eventoSalvo);
            return eventoSalvo;
        } else {
            eventos.put(evento.getId(), evento);
            return evento;
        }
    }

    @Override
    public Optional<Evento> buscarPorId(Long id) {
        return Optional.ofNullable(eventos.get(id));
    }

    @Override
    public List<Evento> listarTodos() {
        return new ArrayList<>(eventos.values());
    }
}
