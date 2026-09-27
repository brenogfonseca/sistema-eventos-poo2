
package br.ueg.eventos.application.service.inscricao;

import br.ueg.eventos.application.port.in.inscricao.ListarInscricoesPorAtividadePort;
import br.ueg.eventos.application.port.out.InscricaoRepositoryPort;
import br.ueg.eventos.application.port.out.AtividadeRepositoryPort;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Inscricao;

import java.util.List;

public class ListarInscricoesPorAtividadeService
        implements ListarInscricoesPorAtividadePort {

    private final InscricaoRepositoryPort inscricaoRepository;
    private final AtividadeRepositoryPort atividadeRepository;

    public ListarInscricoesPorAtividadeService(
            InscricaoRepositoryPort inscricaoRepository,
            AtividadeRepositoryPort atividadeRepository
    ) {
        this.inscricaoRepository = inscricaoRepository;
        this.atividadeRepository = atividadeRepository;
    }

    @Override
    public List<Inscricao> executar(Long idAtividade) {
        if (idAtividade == null || idAtividade <= 0) {
            throw new RegraNegocioException(
                    "O id da atividade deve ser válido."
            );
        }

        atividadeRepository.buscarPorId(idAtividade)
                .orElseThrow(() -> new RegraNegocioException(
                        "Atividade não encontrada."
                ));

        return inscricaoRepository.listarPorAtividade(idAtividade);
    }
}