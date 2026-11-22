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
                    atividade.getTrilha(),
                    atividade.getMetodoIdentificacaoCheckin()
            );
            atividadeSalva.alterarDescricao(atividade.getDescricao());

            atividades.put(novoId, atividadeSalva);
            return atividadeSalva;
        } else {
            atividade.alterarDescricao(atividade.getDescricao());
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
    public List<Atividade> listarPorLocal(Long idLocal) {
        if (idLocal == null) return List.of();
        List<Atividade> resultado = new ArrayList<>();
        for (Atividade atividade : atividades.values()) {
            if (atividade.getLocal() != null && idLocal.equals(atividade.getLocal().getId())) {
                resultado.add(atividade);
            }
        }
        return resultado;
    }

    @Override
    public void excluir(Long id) {
        atividades.remove(id);
    }

    @Override
    public synchronized boolean consumirVaga(Long id) {
        Atividade atividade = atividades.get(id);
        if (atividade == null) throw new IllegalStateException("Atividade não encontrada.");
        if (!atividade.isControlaVagas()) return true;
        if (atividade.getVagas() <= 0) return false;
        atividade.registrarInscricao();
        return true;
    }

    @Override
    public synchronized void liberarVaga(Long id) {
        Atividade atividade = atividades.get(id);
        if (atividade == null) throw new IllegalStateException("Atividade não encontrada.");
        atividade.cancelarInscricao();
    }
}
