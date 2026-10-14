package br.ueg.eventos.application.service.trilha;

import br.ueg.eventos.application.port.in.trilha.DeletarTrilhaPort;
import br.ueg.eventos.application.port.out.TrilhaRepositoryPort;
import br.ueg.eventos.domain.exception.RegraNegocioException;

/** Serviço de exclusão de trilha. */
public class DeletarTrilhaService implements DeletarTrilhaPort {

    private final TrilhaRepositoryPort trilhaRepository;

    public DeletarTrilhaService(TrilhaRepositoryPort trilhaRepository) {
        this.trilhaRepository = trilhaRepository;
    }

    @Override
    public void executar(Long id) {
        if (id == null || id <= 0) {
            throw new RegraNegocioException("ID da trilha inválido.");
        }
        if (trilhaRepository.buscarPorId(id).isEmpty()) {
            throw new RegraNegocioException("Trilha não encontrada.");
        }

        trilhaRepository.deletar(id);
    }
}