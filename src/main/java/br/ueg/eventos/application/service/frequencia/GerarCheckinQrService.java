package br.ueg.eventos.application.service.frequencia;

import br.ueg.eventos.application.port.in.frequencia.GerarCheckinQrPort;
import br.ueg.eventos.application.port.out.AtividadeRepositoryPort;
import br.ueg.eventos.application.port.out.CheckinQrTokenSignerPort;
import br.ueg.eventos.application.port.out.InscricaoRepositoryPort;
import br.ueg.eventos.domain.exception.AcessoNegadoException;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Atividade;
import br.ueg.eventos.domain.model.Inscricao;
import br.ueg.eventos.domain.model.TipoFrequenciaEnum;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

public class GerarCheckinQrService implements GerarCheckinQrPort {

    private static final Duration VALIDADE_TOKEN = Duration.ofMinutes(15);

    private final InscricaoRepositoryPort inscricaoRepository;
    private final AtividadeRepositoryPort atividadeRepository;
    private final CheckinQrTokenSignerPort tokenSigner;
    private final Clock clock;

    public GerarCheckinQrService(
            InscricaoRepositoryPort inscricaoRepository,
            AtividadeRepositoryPort atividadeRepository,
            CheckinQrTokenSignerPort tokenSigner
    ) {
        this(inscricaoRepository, atividadeRepository, tokenSigner, Clock.systemUTC());
    }

    GerarCheckinQrService(
            InscricaoRepositoryPort inscricaoRepository,
            AtividadeRepositoryPort atividadeRepository,
            CheckinQrTokenSignerPort tokenSigner,
            Clock clock
    ) {
        this.inscricaoRepository = inscricaoRepository;
        this.atividadeRepository = atividadeRepository;
        this.tokenSigner = tokenSigner;
        this.clock = clock;
    }

    @Override
    public DadosCheckinQr executar(Long idInscricao, Integer idUsuario) {
        if (idInscricao == null || idInscricao <= 0) {
            throw new RegraNegocioException("O id da inscrição deve ser válido.");
        }
        if (idUsuario == null || idUsuario <= 0) {
            throw new AcessoNegadoException("É necessário estar autenticado para gerar o QR Code.");
        }

        Inscricao inscricao = inscricaoRepository.buscarPorId(idInscricao)
                .orElseThrow(() -> new RegraNegocioException("Inscrição não encontrada."));

        if (!inscricao.estaAtiva()) {
            throw new RegraNegocioException("Não é possível gerar QR Code para uma inscrição cancelada.");
        }
        if (!inscricao.getIdUsuario().equals(idUsuario.longValue())) {
            throw new AcessoNegadoException("A inscrição não pertence ao usuário autenticado.");
        }

        Atividade atividade = atividadeRepository.buscarPorId(inscricao.getIdAtividade())
                .orElseThrow(() -> new RegraNegocioException("Atividade da inscrição não encontrada."));
        if (atividade.getTipoFrequencia() != TipoFrequenciaEnum.CHECKIN) {
            throw new RegraNegocioException(
                    "QR Code está disponível somente para atividades configuradas com frequência CHECKIN."
            );
        }

        Instant expiraEm = clock.instant().plus(VALIDADE_TOKEN);
        String token = tokenSigner.emitir(idInscricao, expiraEm);
        return new DadosCheckinQr(token, expiraEm);
    }
}
