
package br.ueg.eventos.infrastructure.adapter.out.persistence;

import br.ueg.eventos.application.port.out.EventoRepositoryPort;
import br.ueg.eventos.domain.model.Evento;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class InMemoryEventoRepository implements EventoRepositoryPort {

    private final Map<Long, Evento> eventos =
            new ConcurrentHashMap<>();

    private final AtomicLong idGenerator =
            new AtomicLong(1);

    @Override
    public Evento salvar(Evento evento) {

        if (evento == null) {
            throw new IllegalArgumentException(
                    "O evento não pode ser nulo."
            );
        }

        if (evento.getId() == null) {

            Long novoId = idGenerator.getAndIncrement();

            Evento eventoSalvo = copiarEventoComId(
                    evento,
                    novoId
            );

            eventos.put(novoId, eventoSalvo);

            return eventoSalvo;
        }

        Long id = evento.getId();

        if (id <= 0) {
            throw new IllegalArgumentException(
                    "O ID do evento deve ser maior que zero."
            );
        }

        idGenerator.updateAndGet(
                atual -> Math.max(atual, id + 1)
        );

        eventos.put(id, evento);

        return evento;
    }

    @Override
    public Optional<Evento> buscarPorId(Long id) {

        if (id == null || id <= 0) {
            return Optional.empty();
        }

        return Optional.ofNullable(eventos.get(id));
    }

    @Override
    public List<Evento> listarTodos() {

        return new ArrayList<>(eventos.values());
    }

    // Buscar somente eventos não deletados
    public List<Evento> listarAtivos() {

        return eventos.values()
                .stream()
                .filter(evento -> !evento.isDeletado())
                .sorted(Comparator.comparing(Evento::getId))
                .collect(Collectors.toList());
    }

    // Verificar se existe um evento
    public boolean existePorId(Long id) {

        return id != null
                && eventos.containsKey(id);
    }

    // Verificar se o evento existe e não foi deletado
    public boolean existeAtivoPorId(Long id) {

        return buscarPorId(id)
                .filter(evento -> !evento.isDeletado())
                .isPresent();
    }

    // Exclusão lógica
    @Override
    public void deletar(Long id) {
        if (id == null) {
            return;
        }
        Evento evento = eventos.get(id);
        if (evento != null) {
            evento.deletar();
        }
    }

    public boolean deletarPorId(Long id) {

        Evento evento = eventos.get(id);

        if (evento == null || evento.isDeletado()) {
            return false;
        }

        evento.deletar();

        return true;
    }

    // Busca por título
    public List<Evento> buscarPorTitulo(String titulo) {

        if (titulo == null || titulo.isBlank()) {
            return List.of();
        }

        String termo = titulo.trim().toLowerCase();

        return eventos.values()
                .stream()
                .filter(evento -> !evento.isDeletado())
                .filter(evento -> evento.getTitulo()
                        .toLowerCase()
                        .contains(termo))
                .sorted(Comparator.comparing(Evento::getId))
                .collect(Collectors.toList());
    }

    // Busca por eventos com inscrições abertas
    public List<Evento> listarComInscricoesAbertas() {

        return eventos.values()
                .stream()
                .filter(evento -> !evento.isDeletado())
                .filter(Evento::isInscricoesAbertas)
                .sorted(Comparator.comparing(Evento::getId))
                .collect(Collectors.toList());
    }

    // Limpar todos os eventos (útil para testes)
    public void limpar() {

        eventos.clear();

        idGenerator.set(1);
    }

    // Criar uma cópia do evento com ID
    private Evento copiarEventoComId(
            Evento evento,
            Long id) {

        return new Evento(
                id,
                evento.getTitulo(),
                evento.getDescricao(),
                evento.getPeriodo(),
                evento.getCapacidadeMaxima(),
                evento.isInscricoesAbertas(),
                evento.isDeletado(),
                evento.isCertificado(),
                evento.getFrequenciaMinima()
        );
    }
}