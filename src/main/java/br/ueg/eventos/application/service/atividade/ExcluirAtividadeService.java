package br.ueg.eventos.application.service.atividade;

import br.ueg.eventos.application.port.in.atividade.ExcluirAtividadePort;
import br.ueg.eventos.application.port.out.AtividadeRepositoryPort;
import br.ueg.eventos.application.port.out.InscricaoRepositoryPort;
import br.ueg.eventos.domain.exception.RegraNegocioException;

public class ExcluirAtividadeService
        implements ExcluirAtividadePort {

    private final AtividadeRepositoryPort atividadeRepository;
    private final InscricaoRepositoryPort inscricaoRepository;

    public ExcluirAtividadeService(
            AtividadeRepositoryPort atividadeRepository,
            InscricaoRepositoryPort inscricaoRepository) {
        this.atividadeRepository = atividadeRepository;
        this.inscricaoRepository = inscricaoRepository;
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
        if (!inscricaoRepository.listarPorAtividade(id).isEmpty()) {
            throw new RegraNegocioException("Não é possível excluir uma atividade que já possui inscrições. Feche as inscrições ou cancele o evento para preservar o histórico.");
        }

        atividadeRepository.excluir(id);
    }
}
