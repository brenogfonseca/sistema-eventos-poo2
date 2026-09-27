package br.ueg.eventos.application.service.atividade;

import br.ueg.eventos.application.port.in.atividade.BuscarAtividadePorIdPort;
import br.ueg.eventos.application.port.out.AtividadeRepositoryPort;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Atividade;

public class BuscarAtividadePorIdService
        implements BuscarAtividadePorIdPort {

    private final AtividadeRepositoryPort atividadeRepository;

    public BuscarAtividadePorIdService(
            AtividadeRepositoryPort atividadeRepository) {
        this.atividadeRepository = atividadeRepository;
    }

    @Override
    public Atividade executar(Long id) {
        if (id == null || id <= 0) {
            throw new RegraNegocioException(
                    "O id da atividade deve ser válido.");
        }

        return atividadeRepository.buscarPorId(id)
                .orElseThrow(() -> new RegraNegocioException(
                        "Atividade não encontrada."));
    }
}