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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Calcula a frequência do evento; cada bloco de atividades simultâneas usa sua maior duração. */
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
        InscricaoEvento inscricaoEvento = inscricaoEventoRepository.buscarPorId(idInscricaoEvento)
                .orElseThrow(() -> new RegraNegocioException("Inscrição no evento não encontrada."));
        Evento evento = eventoRepository.buscarPorId(inscricaoEvento.getIdEvento())
                .orElseThrow(() -> new RegraNegocioException("Evento não encontrado."));
        if (!inscricaoEvento.estaAtiva()) throw new RegraNegocioException("A inscrição no evento está cancelada.");
        if (idUsuario == null || !inscricaoEvento.getIdUsuario().equals(idUsuario)) {
            throw new RegraNegocioException("Só é permitido consultar a própria frequência.");
        }

        Map<Long, Inscricao> selecoesAtivas = new HashMap<>();
        for (Inscricao selecao : inscricaoRepository.listarPorInscricaoEvento(inscricaoEvento.getId())) {
            if (selecao.estaAtiva()) selecoesAtivas.put(selecao.getIdAtividade(), selecao);
        }

        List<IntervaloAtividade> intervalos = atividadeRepository.listarPorEvento(evento.getId()).stream()
                .map(IntervaloAtividade::de)
                .sorted(Comparator.comparing(IntervaloAtividade::inicio))
                .toList();
        long total = 0;
        long presentes = 0;
        List<IntervaloAtividade> bloco = new ArrayList<>();
        LocalDateTime fimBloco = null;
        for (IntervaloAtividade atual : intervalos) {
            if (!bloco.isEmpty() && !atual.inicio().isBefore(fimBloco)) {
                long[] carga = computarBloco(bloco, selecoesAtivas);
                total += carga[0];
                presentes += carga[1];
                bloco.clear();
                fimBloco = null;
            }
            bloco.add(atual);
            if (fimBloco == null || atual.fim().isAfter(fimBloco)) fimBloco = atual.fim();
        }
        if (!bloco.isEmpty()) {
            long[] carga = computarBloco(bloco, selecoesAtivas);
            total += carga[0];
            presentes += carga[1];
        }

        double percentual = total == 0 ? 0.0 : Math.round((presentes * 10000.0) / total) / 100.0;
        boolean criterioAtendido = "PERCENTUAL".equals(evento.getCriterioFrequenciaCertificado())
                ? BigDecimal.valueOf(presentes).multiply(BigDecimal.valueOf(100))
                        .compareTo(BigDecimal.valueOf(total)
                                .multiply(BigDecimal.valueOf(evento.getFrequenciaMinima()))) >= 0
                : presentes >= evento.getFrequenciaMinimaMinutos();
        boolean elegivel = evento.isCertificado() && total > 0 && criterioAtendido;
        long cargaCertificado = "FIXA".equals(evento.getModoCargaHorariaCertificado())
                ? evento.getCargaHorariaFixaMinutos() : presentes;
        return new Resultado(evento.isCertificado(), evento.getFrequenciaMinima(),
                evento.getCriterioFrequenciaCertificado(), evento.getFrequenciaMinimaMinutos(),
                evento.getModoCargaHorariaCertificado(), cargaCertificado,
                percentual, presentes, total, elegivel);
    }

    private long[] computarBloco(List<IntervaloAtividade> bloco, Map<Long, Inscricao> selecoesAtivas) {
        long duracaoMaxima = 0;
        boolean presenteNoBloco = false;
        for (IntervaloAtividade intervalo : bloco) {
            duracaoMaxima = Math.max(duracaoMaxima, intervalo.minutos());
            Inscricao selecao = selecoesAtivas.get(intervalo.atividade().getId());
            if (selecao != null && validada(selecao, intervalo.atividade())) presenteNoBloco = true;
        }
        return new long[]{duracaoMaxima, presenteNoBloco ? duracaoMaxima : 0};
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

    private record IntervaloAtividade(Atividade atividade, LocalDateTime inicio, LocalDateTime fim, long minutos) {
        private static IntervaloAtividade de(Atividade atividade) {
            LocalDateTime inicio = LocalDateTime.of(atividade.getData(), atividade.getHoraInicio());
            LocalDateTime fim = LocalDateTime.of(atividade.getData(), atividade.getHoraFim());
            if (fim.isBefore(inicio)) fim = fim.plusDays(1);
            return new IntervaloAtividade(atividade, inicio, fim,
                    Math.max(0, ChronoUnit.MINUTES.between(inicio, fim)));
        }
    }
}
