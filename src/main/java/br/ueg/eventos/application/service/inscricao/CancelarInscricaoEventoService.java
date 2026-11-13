package br.ueg.eventos.application.service.inscricao;

import br.ueg.eventos.application.port.in.inscricao.CancelarInscricaoEventoPort;
import br.ueg.eventos.application.port.out.AtividadeRepositoryPort;
import br.ueg.eventos.application.port.out.InscricaoEventoRepositoryPort;
import br.ueg.eventos.application.port.out.InscricaoRepositoryPort;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Atividade;
import br.ueg.eventos.domain.model.Inscricao;
import br.ueg.eventos.domain.model.InscricaoEvento;

public class CancelarInscricaoEventoService implements CancelarInscricaoEventoPort {
    private final InscricaoEventoRepositoryPort inscricaoEventoRepository;
    private final InscricaoRepositoryPort inscricaoRepository;
    private final AtividadeRepositoryPort atividadeRepository;

    public CancelarInscricaoEventoService(InscricaoEventoRepositoryPort inscricaoEventoRepository,
                                          InscricaoRepositoryPort inscricaoRepository,
                                          AtividadeRepositoryPort atividadeRepository) {
        this.inscricaoEventoRepository = inscricaoEventoRepository;
        this.inscricaoRepository = inscricaoRepository;
        this.atividadeRepository = atividadeRepository;
    }

    @Override
    public void executar(Long idInscricaoEvento, Long idUsuario) {
        InscricaoEvento inscricaoEvento = inscricaoEventoRepository.buscarPorId(idInscricaoEvento)
                .orElseThrow(() -> new RegraNegocioException("Inscrição no evento não encontrada."));
        if (idUsuario == null || !idUsuario.equals(inscricaoEvento.getIdUsuario())) {
            throw new RegraNegocioException("Só é permitido cancelar a própria inscrição.");
        }
        if (!inscricaoEvento.estaAtiva()) throw new RegraNegocioException("A inscrição no evento já está cancelada.");

        for (Inscricao inscricaoAtividade : inscricaoRepository.listarPorInscricaoEvento(idInscricaoEvento)) {
            if (!inscricaoAtividade.estaAtiva()) continue;
            Atividade atividade = atividadeRepository.buscarPorId(inscricaoAtividade.getIdAtividade())
                    .orElseThrow(() -> new RegraNegocioException("Atividade escolhida não encontrada."));
            inscricaoAtividade.cancelar();
            atividade.cancelarInscricao();
            atividadeRepository.salvar(atividade);
            inscricaoRepository.salvar(inscricaoAtividade);
        }
        inscricaoEvento.cancelar();
        inscricaoEventoRepository.salvar(inscricaoEvento);
    }
}
