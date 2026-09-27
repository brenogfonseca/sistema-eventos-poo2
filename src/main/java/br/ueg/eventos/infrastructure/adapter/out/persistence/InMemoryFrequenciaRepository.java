package br.ueg.eventos.infrastructure.adapter.out.persistence;

import br.ueg.eventos.application.port.out.FrequenciaRepositoryPort;
import br.ueg.eventos.domain.model.Frequencia;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class InMemoryFrequenciaRepository
        implements FrequenciaRepositoryPort {

    private final Map<Integer, Frequencia> frequencias =
            new ConcurrentHashMap<>();

    private final AtomicInteger idGenerator = new AtomicInteger(1);

    @Override
    public Frequencia salvar(Frequencia frequencia) {
        if (frequencia.getId() == null) {
            Integer novoId = idGenerator.getAndIncrement();

            Frequencia frequenciaSalva = new Frequencia(
                    novoId,
                    frequencia.getDataHora(),
                    frequencia.getOrigem(),
                    frequencia.getIdResponsavel(),
                    frequencia.isPresente(),
                    frequencia.getIdInscricao(),
                    frequencia.getTipo()
            );

            frequencias.put(novoId, frequenciaSalva);
            return frequenciaSalva;
        }

        frequencias.put(frequencia.getId(), frequencia);
        return frequencia;
    }

    @Override
    public Optional<Frequencia> buscarPorId(Integer id) {
        return Optional.ofNullable(frequencias.get(id));
    }

    @Override
    public List<Frequencia> listarPorInscricao(Integer idInscricao) {
        return frequencias.values()
                .stream()
                .filter(frequencia ->
                        frequencia.getIdInscricao().equals(idInscricao)
                )
                .collect(Collectors.toCollection(ArrayList::new));
    }
}