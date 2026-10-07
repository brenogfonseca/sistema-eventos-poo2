package br.ueg.eventos.infrastructure.adapter.out.persistence;

import br.ueg.eventos.application.port.out.VinculoOrganizadorRepositoryPort;
import br.ueg.eventos.domain.model.VinculoOrganizador;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Adaptador de persistência em memória para os vínculos de organizadores com eventos.
 * Mantém os registros thread-safe em memória durante o ciclo de execução.
 */
public class InMemoryVinculoOrganizadorRepository implements VinculoOrganizadorRepositoryPort {

    // Armazenamento em memória indexado por ID do vínculo
    private final Map<Long, VinculoOrganizador> vinculos = new ConcurrentHashMap<>();

    // Gerador atômico de chaves primárias
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public VinculoOrganizador salvar(VinculoOrganizador vinculo) {
        if (vinculo == null) {
            throw new IllegalArgumentException("O vínculo do organizador não pode ser nulo.");
        }

        // Se for um novo registro sem ID gerado
        if (vinculo.getId() == null) {
            Long novoId = idGenerator.getAndIncrement();
            VinculoOrganizador novoVinculo = new VinculoOrganizador(
                    novoId,
                    vinculo.getUsuarioId(),
                    vinculo.getEventoId(),
                    vinculo.isCriadorPrincipal()
            );
            vinculos.put(novoId, novoVinculo);
            return novoVinculo;
        }

        // Se já possuir ID, atualiza no mapa
        vinculos.put(vinculo.getId(), vinculo);
        return vinculo;
    }

    @Override
    public Optional<VinculoOrganizador> buscarPorUsuarioEEvento(Integer usuarioId, Integer eventoId) {
        if (usuarioId == null || eventoId == null) {
            return Optional.empty();
        }

        return vinculos.values().stream()
                .filter(v -> v.getUsuarioId().equals(usuarioId) && v.getEventoId().equals(eventoId))
                .findFirst();
    }

    @Override
    public List<VinculoOrganizador> listarPorEvento(Integer eventoId) {
        if (eventoId == null) {
            return List.of();
        }

        return vinculos.values().stream()
                .filter(v -> v.getEventoId().equals(eventoId))
                .collect(Collectors.toList());
    }

    @Override
    public List<VinculoOrganizador> listarPorUsuario(Integer usuarioId) {
        if (usuarioId == null) {
            return List.of();
        }

        return vinculos.values().stream()
                .filter(v -> v.getUsuarioId().equals(usuarioId))
                .collect(Collectors.toList());
    }

    @Override
    public void deletarPorEvento(Integer eventoId) {
        if (eventoId == null) {
            return;
        }

        List<Long> idsParaRemover = vinculos.values().stream()
                .filter(v -> v.getEventoId().equals(eventoId))
                .map(VinculoOrganizador::getId)
                .collect(Collectors.toList());

        idsParaRemover.forEach(vinculos::remove);
    }
}
