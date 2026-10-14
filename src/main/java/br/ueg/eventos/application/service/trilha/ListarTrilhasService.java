package br.ueg.eventos.application.service.trilha;

import br.ueg.eventos.application.port.in.trilha.ListarTrilhasPort;
import br.ueg.eventos.application.port.out.TrilhaRepositoryPort;
import br.ueg.eventos.domain.model.Trilha;

import java.util.List;

/** Serviço de listagem de trilhas. */
public class ListarTrilhasService implements ListarTrilhasPort {

    private final TrilhaRepositoryPort trilhaRepository;

    public ListarTrilhasService(TrilhaRepositoryPort trilhaRepository) {
        this.trilhaRepository = trilhaRepository;
    }

    @Override
    public List<Trilha> executar() {
        return trilhaRepository.listarTodos();
    }
}