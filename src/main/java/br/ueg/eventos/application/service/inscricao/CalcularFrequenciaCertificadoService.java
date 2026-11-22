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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/** Calcula carga do evento pela união dos horários e crédito individual pelas atividades validadas. */
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
        Map<Long, Boolean> presencaValidada = new HashMap<>();
        for (IntervaloAtividade intervalo : intervalos) {
            Inscricao selecao = selecoesAtivas.get(intervalo.atividade().getId());
            if (selecao != null) {
                presencaValidada.put(intervalo.atividade().getId(), validada(selecao, intervalo.atividade()));
            }
        }

        // Divide a programação em trechos delimitados por início/fim. Cada minuto
        // de relógio conta uma vez no evento, mesmo se houver atividades simultâneas.
        // O participante recebe crédito nos trechos de uma atividade que escolheu
        // e cuja presença foi validada. Períodos diferentes são somados.
        TreeSet<LocalDateTime> limites = new TreeSet<>();
        intervalos.forEach(i -> { limites.add(i.inicio()); limites.add(i.fim()); });
        List<LocalDateTime> pontos = List.copyOf(limites);
        long total = 0;
        long presentes = 0;
        for (int i = 0; i + 1 < pontos.size(); i++) {
            LocalDateTime inicioTrecho = pontos.get(i);
            LocalDateTime fimTrecho = pontos.get(i + 1);
            long minutosTrecho = ChronoUnit.MINUTES.between(inicioTrecho, fimTrecho);
            if (minutosTrecho <= 0) continue;

            boolean existeAtividade = intervalos.stream().anyMatch(intervalo -> cobre(
                    intervalo, inicioTrecho, fimTrecho));
            if (!existeAtividade) continue;
            total += minutosTrecho;

            boolean participantePresente = intervalos.stream().anyMatch(intervalo ->
                    Boolean.TRUE.equals(presencaValidada.get(intervalo.atividade().getId()))
                            && cobre(intervalo, inicioTrecho, fimTrecho));
            if (participantePresente) presentes += minutosTrecho;
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

    private boolean cobre(IntervaloAtividade intervalo, LocalDateTime inicio, LocalDateTime fim) {
        return !intervalo.inicio().isAfter(inicio) && !intervalo.fim().isBefore(fim);
    }

    private boolean validada(Inscricao inscricao, Atividade atividade) {
        List<Frequencia> marcacoes = frequenciaRepository.listarPorInscricao(Math.toIntExact(inscricao.getId()));
        var correcaoOrganizador = marcacoes.stream()
                .filter(f -> "CORRECAO_ORGANIZADOR".equalsIgnoreCase(f.getOrigem()))
                .max(Comparator.comparing(Frequencia::getDataHora)
                        .thenComparing(f -> f.getId() == null ? 0 : f.getId()));
        if (correcaoOrganizador.isPresent()) return correcaoOrganizador.get().isPresente();
        TipoFrequenciaEnum politica = atividade.getTipoFrequencia();
        if (politica == TipoFrequenciaEnum.CHECKIN) {
            return marcacoes.stream().anyMatch(f -> f.isPresente() && "CHECKIN".equalsIgnoreCase(f.getTipo())
                    && !"CORRECAO_ORGANIZADOR".equalsIgnoreCase(f.getOrigem()));
        }
        if (politica == TipoFrequenciaEnum.ENTRADA_SAIDA) {
            boolean entrada = marcacoes.stream().anyMatch(f -> f.isPresente()
                    && List.of("QR_ENTRADA", "BUSCA_ENTRADA").contains(f.getOrigem().toUpperCase()));
            boolean saida = marcacoes.stream().anyMatch(f -> f.isPresente()
                    && List.of("QR_SAIDA", "BUSCA_SAIDA").contains(f.getOrigem().toUpperCase()));
            return entrada && saida;
        }
        return marcacoes.stream()
                .filter(f -> "MANUAL".equalsIgnoreCase(f.getTipo()))
                .max(Comparator.comparing(Frequencia::getDataHora)
                        .thenComparing(f -> f.getId() == null ? 0 : f.getId()))
                .map(Frequencia::isPresente)
                .orElse(false);
    }

    private record IntervaloAtividade(Atividade atividade, LocalDateTime inicio, LocalDateTime fim) {
        private static IntervaloAtividade de(Atividade atividade) {
            LocalDateTime inicio = LocalDateTime.of(atividade.getData(), atividade.getHoraInicio());
            LocalDateTime fim = LocalDateTime.of(atividade.getData(), atividade.getHoraFim());
            if (fim.isBefore(inicio)) fim = fim.plusDays(1);
            return new IntervaloAtividade(atividade, inicio, fim);
        }
    }
}
