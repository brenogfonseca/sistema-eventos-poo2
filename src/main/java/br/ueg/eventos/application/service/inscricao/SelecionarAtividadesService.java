package br.ueg.eventos.application.service.inscricao;

import br.ueg.eventos.application.port.in.inscricao.SelecionarAtividadesPort;
import br.ueg.eventos.application.port.out.AtividadeRepositoryPort;
import br.ueg.eventos.application.port.out.EventoRepositoryPort;
import br.ueg.eventos.application.port.out.InscricaoEventoRepositoryPort;
import br.ueg.eventos.application.port.out.InscricaoRepositoryPort;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Atividade;
import br.ueg.eventos.domain.model.Evento;
import br.ueg.eventos.domain.model.Inscricao;
import br.ueg.eventos.domain.model.InscricaoEvento;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SelecionarAtividadesService implements SelecionarAtividadesPort {
    private final InscricaoEventoRepositoryPort inscricaoEventoRepository;
    private final InscricaoRepositoryPort inscricaoRepository;
    private final AtividadeRepositoryPort atividadeRepository;
    private final EventoRepositoryPort eventoRepository;

    public SelecionarAtividadesService(InscricaoEventoRepositoryPort inscricaoEventoRepository,
                                      InscricaoRepositoryPort inscricaoRepository,
                                      AtividadeRepositoryPort atividadeRepository,
                                      EventoRepositoryPort eventoRepository) {
        this.inscricaoEventoRepository = inscricaoEventoRepository;
        this.inscricaoRepository = inscricaoRepository;
        this.atividadeRepository = atividadeRepository;
        this.eventoRepository = eventoRepository;
    }

    @Override
    public List<Inscricao> executar(Long idInscricaoEvento, Long idUsuario, List<Long> idsAtividades) {
        if (idInscricaoEvento == null || idInscricaoEvento <= 0 || idUsuario == null || idUsuario <= 0) {
            throw new RegraNegocioException("Inscrição no evento e usuário devem ser válidos.");
        }
        if (idsAtividades == null) throw new RegraNegocioException("A lista de atividades é obrigatória.");
        InscricaoEvento inscricaoEvento = inscricaoEventoRepository.buscarPorId(idInscricaoEvento)
                .orElseThrow(() -> new RegraNegocioException("Inscrição no evento não encontrada."));
        if (!inscricaoEvento.getIdUsuario().equals(idUsuario)) {
            throw new RegraNegocioException("A inscrição no evento não pertence ao usuário informado.");
        }
        if (!inscricaoEvento.estaAtiva()) {
            throw new RegraNegocioException("A inscrição no evento está cancelada.");
        }
        Evento evento = eventoRepository.buscarPorId(inscricaoEvento.getIdEvento())
                .orElseThrow(() -> new RegraNegocioException("Evento não encontrado."));
        if (evento.isDeletado() || !evento.isInscricoesAbertas()) {
            throw new RegraNegocioException("A escolha de atividades está indisponível porque as inscrições do evento estão fechadas.");
        }

        Set<Long> unicas = new HashSet<>(idsAtividades);
        if (unicas.contains(null) || unicas.size() != idsAtividades.size()) {
            throw new RegraNegocioException("A lista contém atividades inválidas ou repetidas.");
        }

        List<Atividade> atividadesAlvo = new ArrayList<>();
        Map<Long, Inscricao> inscricoesAtivas = new LinkedHashMap<>();
        for (Inscricao existente : inscricaoRepository.listarPorInscricaoEvento(idInscricaoEvento)) {
            if (existente.estaAtiva()) inscricoesAtivas.put(existente.getIdAtividade(), existente);
        }
        for (Long idAtividade : idsAtividades) {
            Atividade atividade = atividadeRepository.buscarPorId(idAtividade)
                    .orElseThrow(() -> new RegraNegocioException("Atividade " + idAtividade + " não encontrada."));
            if (!atividade.getEventoId().equals(inscricaoEvento.getIdEvento())) {
                throw new RegraNegocioException("Todas as atividades selecionadas devem pertencer ao evento inscrito.");
            }
            if (!inscricoesAtivas.containsKey(idAtividade)
                    && atividade.isControlaVagas() && atividade.getVagas() <= 0) {
                throw new RegraNegocioException("Não há vagas disponíveis para a atividade " + atividade.getNome() + ".");
            }
            LocalDateTime inicio = LocalDateTime.of(atividade.getData(), atividade.getHoraInicio());
            if (!inscricoesAtivas.containsKey(idAtividade) && !inicio.isAfter(LocalDateTime.now())) {
                throw new RegraNegocioException("Não é possível escolher a atividade '" + atividade.getNome() + "' depois que ela começou.");
            }
            atividadesAlvo.add(atividade);
        }
        for (int i = 0; i < atividadesAlvo.size(); i++) {
            for (int j = 0; j < i; j++) verificarConflito(atividadesAlvo.get(i), atividadesAlvo.get(j));
        }
        for (Inscricao existente : inscricoesAtivas.values()) {
            if (!unicas.contains(existente.getIdAtividade())) {
                Atividade atividade = atividadeRepository.buscarPorId(existente.getIdAtividade())
                        .orElseThrow(() -> new RegraNegocioException("Atividade escolhida não encontrada."));
                LocalDateTime inicio = LocalDateTime.of(atividade.getData(), atividade.getHoraInicio());
                if (!LocalDateTime.now().isBefore(inicio)) {
                    throw new RegraNegocioException("Não é possível remover a atividade '" + atividade.getNome() + "' depois que ela começou.");
                }
            }
        }

        List<Inscricao> selecionadas = new ArrayList<>();
        for (Inscricao existente : inscricoesAtivas.values()) {
            if (!unicas.contains(existente.getIdAtividade())) {
                Atividade atividade = atividadeRepository.buscarPorId(existente.getIdAtividade())
                        .orElseThrow(() -> new RegraNegocioException("Atividade escolhida não encontrada."));
                existente.cancelar();
                atividade.cancelarInscricao();
                atividadeRepository.salvar(atividade);
                inscricaoRepository.salvar(existente);
            }
        }
        for (Atividade atividade : atividadesAlvo) {
            Inscricao existente = inscricoesAtivas.get(atividade.getId());
            if (existente != null) {
                selecionadas.add(existente);
                continue;
            }
            atividade.registrarInscricao();
            atividadeRepository.salvar(atividade);
            selecionadas.add(inscricaoRepository.salvar(new Inscricao(
                    atividade.getId(), inscricaoEvento.getIdUsuario(), idInscricaoEvento)));
        }
        return selecionadas;
    }

    private void verificarConflito(Atividade a, Atividade b) {
        LocalDateTime inicioA = LocalDateTime.of(a.getData(), a.getHoraInicio());
        LocalDateTime fimA = LocalDateTime.of(a.getData(), a.getHoraFim());
        LocalDateTime inicioB = LocalDateTime.of(b.getData(), b.getHoraInicio());
        LocalDateTime fimB = LocalDateTime.of(b.getData(), b.getHoraFim());
        if (fimA.isBefore(inicioA)) fimA = fimA.plusDays(1);
        if (fimB.isBefore(inicioB)) fimB = fimB.plusDays(1);
        if (inicioA.isBefore(fimB) && inicioB.isBefore(fimA)) {
            throw new RegraNegocioException("A atividade '" + a.getNome()
                    + "' conflita no horário com '" + b.getNome() + "'.");
        }
    }
}
