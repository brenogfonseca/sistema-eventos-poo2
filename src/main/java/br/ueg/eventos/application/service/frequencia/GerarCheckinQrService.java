package br.ueg.eventos.application.service.frequencia;

import br.ueg.eventos.application.port.in.frequencia.GerarCheckinQrPort;
import br.ueg.eventos.application.port.out.CheckinQrTokenSignerPort;
import br.ueg.eventos.application.port.out.EventoRepositoryPort;
import br.ueg.eventos.application.port.out.InscricaoEventoRepositoryPort;
import br.ueg.eventos.domain.exception.AcessoNegadoException;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Evento;
import br.ueg.eventos.domain.model.InscricaoEvento;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

public class GerarCheckinQrService implements GerarCheckinQrPort {
    private final InscricaoEventoRepositoryPort inscricaoRepository;
    private final EventoRepositoryPort eventoRepository;
    private final CheckinQrTokenSignerPort tokenSigner;
    private final Clock clock;

    public GerarCheckinQrService(InscricaoEventoRepositoryPort inscricaoRepository,
                                 EventoRepositoryPort eventoRepository,
                                 CheckinQrTokenSignerPort tokenSigner) {
        this(inscricaoRepository, eventoRepository, tokenSigner, Clock.systemUTC());
    }

    GerarCheckinQrService(InscricaoEventoRepositoryPort inscricaoRepository,
                          EventoRepositoryPort eventoRepository,
                          CheckinQrTokenSignerPort tokenSigner, Clock clock) {
        this.inscricaoRepository = inscricaoRepository;
        this.eventoRepository = eventoRepository;
        this.tokenSigner = tokenSigner;
        this.clock = clock;
    }

    @Override
    public DadosCheckinQr executar(Long idInscricaoEvento, Integer idUsuario) {
        if (idInscricaoEvento == null || idInscricaoEvento <= 0) {
            throw new RegraNegocioException("O id da inscrição no evento deve ser válido.");
        }
        if (idUsuario == null || idUsuario <= 0) {
            throw new AcessoNegadoException("É necessário autenticar para gerar o QR Code.");
        }
        InscricaoEvento inscricao = inscricaoRepository.buscarPorId(idInscricaoEvento)
                .orElseThrow(() -> new RegraNegocioException("Inscrição no evento não encontrada."));
        if (!inscricao.estaAtiva()) throw new RegraNegocioException("A inscrição no evento está cancelada.");
        if (!inscricao.getIdUsuario().equals(idUsuario.longValue())) {
            throw new AcessoNegadoException("A inscrição no evento não pertence ao usuário autenticado.");
        }
        Evento evento = eventoRepository.buscarPorId(inscricao.getIdEvento())
                .orElseThrow(() -> new RegraNegocioException("Evento da inscrição não encontrado."));
        Instant expiraEm = evento.getPeriodo().getFim()
                .atZone(ZoneId.systemDefault()).toInstant();
        if (!expiraEm.isAfter(clock.instant())) {
            throw new RegraNegocioException("O evento já terminou; o QR Code não pode mais ser emitido.");
        }
        return new DadosCheckinQr(tokenSigner.emitir(inscricao.getId(), expiraEm), expiraEm);
    }
}
