package br.ueg.eventos.infrastructure.adapter.out.persistence;

import br.ueg.eventos.application.port.out.AtividadeRepositoryPort;
import br.ueg.eventos.domain.model.Atividade;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryAtividadeRepository
        implements AtividadeRepositoryPort {

    private final Map<Long, Atividade> atividades =
            new ConcurrentHashMap<>();

    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public Atividade salvar(Atividade atividade) {
        if (atividade.getId() == null) {
            Long novoId = idGenerator.getAndIncrement();

            Atividade atividadeSalva = new Atividade(
                    novoId,
                    atividade.getNome(),
                    atividade.getEventoId(),
                    atividade.getLocal(),
                    atividade.getHoraInicio(),
                    atividade.getHoraFim(),
                    atividade.getData(),
                    atividade.isControlaVagas(),
                    atividade.getVagas(),
                    atividade.getTipo(),
                    atividade.getTipoFrequencia(),
                    atividade.getTrilha()
            );

            atividades.put(novoId, atividadeSalva);
            return atividadeSalva;
        } else {
            atividades.put(atividade.getId(), atividade);
            return atividade;
        }
    }

    @Override
    public Optional<Atividade> buscarPorId(Long id) {
        return Optional.ofNullable(atividades.get(id));
    }

    @Override
    public List<Atividade> listarPorEvento(Long idEvento) {
        List<Atividade> resultado = new ArrayList<>();

        for (Atividade atividade : atividades.values()) {
            if (atividade.getEventoId().equals(idEvento)) {
                resultado.add(atividade);
            }
        }

        return resultado;
    }

    @Override
    public void excluir(Long id) {
        atividades.remove(id);
    }
}