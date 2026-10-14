package br.ueg.eventos.application.service.trilha;

import br.ueg.eventos.application.port.in.trilha.BuscarTrilhaPort;
import br.ueg.eventos.application.port.out.TrilhaRepositoryPort;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Trilha;

/** Serviço de busca de trilha por ID. */
public class BuscarTrilhaService implements BuscarTrilhaPort {

    private final TrilhaRepositoryPort trilhaRepository;

    public BuscarTrilhaService(TrilhaRepositoryPort trilhaRepository) {
        this.trilhaRepository = trilhaRepository;
    }

    @Override
    public Trilha executar(Long id) {
        if (id == null || id <= 0) {
            throw new RegraNegocioException("ID da trilha inválido.");
        }

        return trilhaRepository.buscarPorId(id)
                .orElseThrow(() ->
                        new RegraNegocioException("Trilha não encontrada."));
    }
}