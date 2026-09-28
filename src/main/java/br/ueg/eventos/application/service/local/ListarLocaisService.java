package br.ueg.eventos.application.service.local;

import br.ueg.eventos.application.port.in.local.ListarLocaisPort;
import br.ueg.eventos.application.port.out.LocalRepositoryPort;
import br.ueg.eventos.domain.model.Local;

import java.util.List;

/**
 * Serviço de aplicação responsável por listar todos os locais.
 */
public class ListarLocaisService implements ListarLocaisPort {

    // Porta de saída injetada para buscar os dados
    private final LocalRepositoryPort localRepository;

    public ListarLocaisService(LocalRepositoryPort localRepository) {
        this.localRepository = localRepository;
    }

    @Override
    public List<Local> executar() {
        // Repassa a chamada ao repositório
        return localRepository.listarTodos();
    }
}
