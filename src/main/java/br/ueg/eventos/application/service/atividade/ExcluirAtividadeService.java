package br.ueg.eventos.application.service.atividade;

import br.ueg.eventos.application.port.in.atividade.ExcluirAtividadePort;
import br.ueg.eventos.application.port.out.AtividadeRepositoryPort;
import br.ueg.eventos.domain.exception.RegraNegocioException;

public class ExcluirAtividadeService
        implements ExcluirAtividadePort {

    private final AtividadeRepositoryPort atividadeRepository;

    public ExcluirAtividadeService(
            AtividadeRepositoryPort atividadeRepository) {
        this.atividadeRepository = atividadeRepository;
    }

    @Override
    public void executar(Long id) {
        if (id == null || id <= 0) {
            throw new RegraNegocioException(
                    "O id da atividade deve ser válido.");
        }

        atividadeRepository.buscarPorId(id)
                .orElseThrow(() -> new RegraNegocioException(
                        "Atividade não encontrada."));

        atividadeRepository.excluir(id);
    }
}