package br.ueg.eventos.application.service.inscricao;

import br.ueg.eventos.application.port.in.inscricao.CalcularFrequenciaCertificadoPort;
import br.ueg.eventos.application.port.out.AtividadeRepositoryPort;
import br.ueg.eventos.application.port.out.EventoRepositoryPort;
import br.ueg.eventos.application.port.out.FrequenciaRepositoryPort;
import br.ueg.eventos.application.port.out.InscricaoEventoRepositoryPort;
import br.ueg.eventos.application.port.out.InscricaoRepositoryPort;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Atividade;
import br.ueg.eventos.domain.model.Evento;
import br.ueg.eventos.domain.model.Frequencia;
import br.ueg.eventos.domain.model.Inscricao;
import br.ueg.eventos.domain.model.InscricaoEvento;
import br.ueg.eventos.domain.model.TipoFrequenciaEnum;

import java.time.temporal.ChronoUnit;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/** Calcula frequência como proporção das horas das atividades escolhidas que foram validadas. */
public class CalcularFrequenciaCertificadoService implements CalcularFrequenciaCertificadoPort {
    private final InscricaoEventoRepositoryPort inscricaoEventoRepository;
    private final InscricaoRepositoryPort inscricaoRepository;
    private final EventoRepositoryPort eventoRepository;
    private final AtividadeRepositoryPort atividadeRepository;
    private final FrequenciaRepositoryPort frequenciaRepository;

    public CalcularFrequenciaCertificadoService(InscricaoEventoRepositoryPort inscricaoEventoRepository,
                                                InscricaoRepositoryPort inscricaoRepository,
                                                EventoRepositoryPort eventoRepository,
                                                AtividadeRepositoryPort atividadeRepository,
                                                FrequenciaRepositoryPort frequenciaRepository) {
        this.inscricaoEventoRepository = inscricaoEventoRepository;
        this.inscricaoRepository = inscricaoRepository;
        this.eventoRepository = eventoRepository;
        this.atividadeRepository = atividadeRepository;
        this.frequenciaRepository = frequenciaRepository;
    }

    @Override
    public Resultado executar(Long idInscricaoEvento, Long idUsuario) {
        InscricaoEvento inscricao = inscricaoEventoRepository.buscarPorId(idInscricaoEvento)
                .orElseThrow(() -> new RegraNegocioException("Inscrição no evento não encontrada."));
        Evento evento = eventoRepository.buscarPorId(inscricao.getIdEvento())
                .orElseThrow(() -> new RegraNegocioException("Evento não encontrado."));
        if (!inscricao.estaAtiva()) throw new RegraNegocioException("A inscrição no evento está cancelada.");
        if (idUsuario == null || !inscricao.getIdUsuario().equals(idUsuario)) {
            throw new RegraNegocioException("Só é permitido consultar a própria frequência.");
        }

        long total = 0;
        long presentes = 0;
        List<Inscricao> selecoes = inscricaoRepository.listarPorInscricaoEvento(inscricao.getId());
        for (Inscricao selecao : selecoes) {
            if (!selecao.estaAtiva()) continue;
            Atividade atividade = atividadeRepository.buscarPorId(selecao.getIdAtividade())
                    .orElseThrow(() -> new RegraNegocioException("Uma atividade selecionada não foi encontrada."));
            LocalDateTime inicio = LocalDateTime.of(atividade.getData(), atividade.getHoraInicio());
            LocalDateTime fim = LocalDateTime.of(atividade.getData(), atividade.getHoraFim());
            if (fim.isBefore(inicio)) fim = fim.plusDays(1);
            long minutos = Math.max(0, ChronoUnit.MINUTES.between(inicio, fim));
            total += minutos;
            if (validada(selecao, atividade)) presentes += minutos;
        }

        double percentual = total == 0 ? 0.0 : Math.round((presentes * 10000.0) / total) / 100.0;
        boolean elegivel = evento.isCertificado() && total > 0 && percentual >= evento.getFrequenciaMinima();
        return new Resultado(evento.isCertificado(), evento.getFrequenciaMinima(),
                percentual, presentes, total, elegivel);
    }

    private boolean validada(Inscricao inscricao, Atividade atividade) {
        List<Frequencia> marcacoes = frequenciaRepository.listarPorInscricao(Math.toIntExact(inscricao.getId()));
        TipoFrequenciaEnum politica = atividade.getTipoFrequencia();
        if (politica == TipoFrequenciaEnum.CHECKIN) {
            return marcacoes.stream().anyMatch(f -> f.isPresente() && "CHECKIN".equalsIgnoreCase(f.getTipo()));
        }
        if (politica == TipoFrequenciaEnum.ENTRADA_SAIDA) {
            boolean entrada = marcacoes.stream().anyMatch(f -> f.isPresente() && "QR_ENTRADA".equalsIgnoreCase(f.getOrigem()));
            boolean saida = marcacoes.stream().anyMatch(f -> f.isPresente() && "QR_SAIDA".equalsIgnoreCase(f.getOrigem()));
            return entrada && saida;
        }
        return marcacoes.stream()
                .filter(f -> "MANUAL".equalsIgnoreCase(f.getTipo()))
                .max(Comparator.comparing(Frequencia::getDataHora))
                .map(Frequencia::isPresente)
                .orElse(false);
    }
}
