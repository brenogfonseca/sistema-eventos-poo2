
package br.ueg.eventos.application.service.inscricao;

import br.ueg.eventos.application.port.in.inscricao.CancelarInscricaoPort;
import br.ueg.eventos.application.port.out.InscricaoRepositoryPort;
import br.ueg.eventos.application.port.out.AtividadeRepositoryPort;
import br.ueg.eventos.domain.model.Inscricao;
import br.ueg.eventos.domain.model.Atividade;
import br.ueg.eventos.domain.exception.RegraNegocioException;

public class CancelarInscricaoService implements CancelarInscricaoPort {

    private final InscricaoRepositoryPort inscricaoRepository;
    private final AtividadeRepositoryPort atividadeRepository;

    public CancelarInscricaoService(
            InscricaoRepositoryPort inscricaoRepository,
            AtividadeRepositoryPort atividadeRepository
    ) {
        this.inscricaoRepository = inscricaoRepository;
        this.atividadeRepository = atividadeRepository;
    }

    @Override
    public void executar(Long idInscricao) {
        Inscricao inscricao = inscricaoRepository.buscarPorId(idInscricao)
                .orElseThrow(() -> new RegraNegocioException(
                        "Inscrição não encontrada."
                ));

        Atividade atividade = atividadeRepository
                .buscarPorId(inscricao.getIdAtividade())
                .orElseThrow(() -> new RegraNegocioException(
                        "Atividade não encontrada."
                ));

        inscricao.cancelar();
        atividade.cancelarInscricao();

        atividadeRepository.salvar(atividade);
        inscricaoRepository.salvar(inscricao);
    }
}