package br.ueg.eventos.infrastructure.adapter.out.persistence;

import br.ueg.eventos.application.port.out.InscricaoRepositoryPort;
import br.ueg.eventos.domain.model.Inscricao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryInscricaoRepository
        implements InscricaoRepositoryPort {

    private final Map<Long, Inscricao> inscricoes =
            new ConcurrentHashMap<>();

    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public Inscricao salvar(Inscricao inscricao) {
        if (inscricao.getId() == null) {
            Long novoId = idGenerator.getAndIncrement();

            Inscricao inscricaoSalva = new Inscricao(
                    novoId,
                    inscricao.getIdAtividade(),
                    inscricao.getIdUsuario(),
                    inscricao.isCancelada()
            );

            inscricoes.put(novoId, inscricaoSalva);
            return inscricaoSalva;
        } else {
            inscricoes.put(inscricao.getId(), inscricao);
            return inscricao;
        }
    }

    @Override
    public Optional<Inscricao> buscarPorId(Long id) {
        return Optional.ofNullable(inscricoes.get(id));
    }

    @Override
    public List<Inscricao> listarPorAtividade(Long idAtividade) {
        List<Inscricao> resultado = new ArrayList<>();

        for (Inscricao inscricao : inscricoes.values()) {
            if (inscricao.getIdAtividade().equals(idAtividade)) {
                resultado.add(inscricao);
            }
        }

        return resultado;
    }

    @Override
    public List<Inscricao> listarPorUsuario(Long idUsuario) {
        List<Inscricao> resultado = new ArrayList<>();

        for (Inscricao inscricao : inscricoes.values()) {
            if (inscricao.getIdUsuario().equals(idUsuario)) {
                resultado.add(inscricao);
            }
        }

        return resultado;
    }

    @Override
    public void excluir(Long id) {
        inscricoes.remove(id);
    }
}