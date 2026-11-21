package br.ueg.eventos.application.service.inscricao;

import br.ueg.eventos.application.port.in.inscricao.CancelarInscricaoEventoPort;
import br.ueg.eventos.application.port.out.AtividadeRepositoryPort;
import br.ueg.eventos.application.port.out.EventoRepositoryPort;
import br.ueg.eventos.application.port.out.InscricaoEventoRepositoryPort;
import br.ueg.eventos.application.port.out.InscricaoRepositoryPort;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Atividade;
import br.ueg.eventos.domain.model.Inscricao;
import br.ueg.eventos.domain.model.InscricaoEvento;
import java.time.LocalDateTime;

public class CancelarInscricaoEventoService implements CancelarInscricaoEventoPort {
    private final InscricaoEventoRepositoryPort inscricaoEventoRepository;
    private final InscricaoRepositoryPort inscricaoRepository;
    private final AtividadeRepositoryPort atividadeRepository;
    private final EventoRepositoryPort eventoRepository;

    public CancelarInscricaoEventoService(InscricaoEventoRepositoryPort inscricaoEventoRepository,
                                          InscricaoRepositoryPort inscricaoRepository,
                                          AtividadeRepositoryPort atividadeRepository,
                                          EventoRepositoryPort eventoRepository) {
        this.inscricaoEventoRepository = inscricaoEventoRepository;
        this.inscricaoRepository = inscricaoRepository;
        this.atividadeRepository = atividadeRepository;
        this.eventoRepository = eventoRepository;
    }

    @Override
    public void executar(Long idInscricaoEvento, Long idUsuario) {
        InscricaoEvento inscricaoEvento = inscricaoEventoRepository.buscarPorId(idInscricaoEvento)
                .orElseThrow(() -> new RegraNegocioException("Inscrição no evento não encontrada."));
        if (idUsuario == null || !idUsuario.equals(inscricaoEvento.getIdUsuario())) {
            throw new RegraNegocioException("Só é permitido cancelar a própria inscrição.");
        }
        if (!inscricaoEvento.estaAtiva()) throw new RegraNegocioException("A inscrição no evento já está cancelada.");
        var evento = eventoRepository.buscarPorId(inscricaoEvento.getIdEvento())
                .orElseThrow(() -> new RegraNegocioException("Evento não encontrado."));
        LocalDateTime prazo = evento.getPrazoCancelamento() != null
                ? evento.getPrazoCancelamento() : evento.getPeriodo().getInicio();
        if (LocalDateTime.now(java.time.ZoneId.of(evento.getFusoHorario())).isAfter(prazo)) {
            throw new RegraNegocioException("O prazo para cancelar esta inscrição já terminou.");
        }

        for (Inscricao inscricaoAtividade : inscricaoRepository.listarPorInscricaoEvento(idInscricaoEvento)) {
            if (!inscricaoAtividade.estaAtiva()) continue;
            Atividade atividade = atividadeRepository.buscarPorId(inscricaoAtividade.getIdAtividade())
                    .orElseThrow(() -> new RegraNegocioException("Atividade escolhida não encontrada."));
            inscricaoAtividade.cancelar();
            inscricaoRepository.salvar(inscricaoAtividade);
            atividadeRepository.liberarVaga(atividade.getId());
        }
        inscricaoEvento.cancelar();
        inscricaoEventoRepository.salvar(inscricaoEvento);
    }
}
