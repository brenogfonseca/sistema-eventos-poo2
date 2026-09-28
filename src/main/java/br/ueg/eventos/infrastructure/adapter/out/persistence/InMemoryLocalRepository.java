package br.ueg.eventos.infrastructure.adapter.out.persistence;

import br.ueg.eventos.application.port.out.LocalRepositoryPort;
import br.ueg.eventos.domain.model.Local;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Adaptador de persistência em memória para a entidade Local.
 * Implementa a porta de saída (Out Port) definida na camada de Application.
 */
public class InMemoryLocalRepository implements LocalRepositoryPort {

    private final List<Local> locais = new ArrayList<>();
    private final AtomicLong contadorId = new AtomicLong(1);

    /**
     * Salva ou atualiza um Local no repositório em memória.
     * Se o ID for nulo, gera um novo ID.
     */
    @Override
    public Local salvar(Local local) {
        if (local.getId() == null) {
            Local novoLocal = new Local(contadorId.getAndIncrement(), local.getNome());
            locais.add(novoLocal);
            return novoLocal;
        } else {
            locais.removeIf(l -> l.getId().equals(local.getId()));
            locais.add(local);
            return local;
        }
    }

    /**
     * Busca um Local pelo seu identificador único.
     */
    @Override
    public Optional<Local> buscarPorId(Long id) {
        return locais.stream()
                .filter(l -> l.getId().equals(id))
                .findFirst();
    }

    /**
     * Retorna a lista de todos os Locais salvos.
     */
    @Override
    public List<Local> listarTodos() {
        return new ArrayList<>(locais);
    }

    /**
     * Deleta um Local pelo seu identificador único.
     */
    @Override
    public void deletar(Long id) {
        locais.removeIf(l -> l.getId().equals(id));
    }
}
