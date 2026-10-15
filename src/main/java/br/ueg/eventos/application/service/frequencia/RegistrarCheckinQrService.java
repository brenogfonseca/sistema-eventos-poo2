package br.ueg.eventos.application.service.frequencia;

import br.ueg.eventos.application.port.in.frequencia.RegistrarCheckinQrPort;
import br.ueg.eventos.application.port.in.frequencia.RegistrarFrequenciaPort;
import br.ueg.eventos.application.port.in.frequencia.RegistrarFrequenciaPort.ComandoRegistrarFrequencia;
import br.ueg.eventos.application.port.out.AtividadeRepositoryPort;
import br.ueg.eventos.application.port.out.CheckinQrTokenSignerPort;
import br.ueg.eventos.application.port.out.FrequenciaRepositoryPort;
import br.ueg.eventos.application.port.out.InscricaoRepositoryPort;
import br.ueg.eventos.application.port.out.UsuarioRepositoryPort;
import br.ueg.eventos.application.port.out.VinculoOrganizadorRepositoryPort;
import br.ueg.eventos.domain.exception.AcessoNegadoException;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Atividade;
import br.ueg.eventos.domain.model.Frequencia;
import br.ueg.eventos.domain.model.Inscricao;
import br.ueg.eventos.domain.model.Perfil;
import br.ueg.eventos.domain.model.TipoFrequenciaEnum;
import br.ueg.eventos.domain.model.Usuario;

import java.time.Clock;
import java.time.LocalDateTime;

public class RegistrarCheckinQrService implements RegistrarCheckinQrPort {

    private final CheckinQrTokenSignerPort tokenSigner;
    private final InscricaoRepositoryPort inscricaoRepository;
    private final AtividadeRepositoryPort atividadeRepository;
    private final FrequenciaRepositoryPort frequenciaRepository;
    private final UsuarioRepositoryPort usuarioRepository;
    private final VinculoOrganizadorRepositoryPort vinculoOrganizadorRepository;
    private final RegistrarFrequenciaPort registrarFrequenciaPort;
    private final Clock clock;

    public RegistrarCheckinQrService(
            CheckinQrTokenSignerPort tokenSigner,
            InscricaoRepositoryPort inscricaoRepository,
            AtividadeRepositoryPort atividadeRepository,
            FrequenciaRepositoryPort frequenciaRepository,
            UsuarioRepositoryPort usuarioRepository,
            VinculoOrganizadorRepositoryPort vinculoOrganizadorRepository,
            RegistrarFrequenciaPort registrarFrequenciaPort
    ) {
        this(
                tokenSigner,
                inscricaoRepository,
                atividadeRepository,
                frequenciaRepository,
                usuarioRepository,
                vinculoOrganizadorRepository,
                registrarFrequenciaPort,
                Clock.systemDefaultZone()
        );
    }

    RegistrarCheckinQrService(
            CheckinQrTokenSignerPort tokenSigner,
            InscricaoRepositoryPort inscricaoRepository,
            AtividadeRepositoryPort atividadeRepository,
            FrequenciaRepositoryPort frequenciaRepository,
            UsuarioRepositoryPort usuarioRepository,
            VinculoOrganizadorRepositoryPort vinculoOrganizadorRepository,
            RegistrarFrequenciaPort registrarFrequenciaPort,
            Clock clock
    ) {
        this.tokenSigner = tokenSigner;
        this.inscricaoRepository = inscricaoRepository;
        this.atividadeRepository = atividadeRepository;
        this.frequenciaRepository = frequenciaRepository;
        this.usuarioRepository = usuarioRepository;
        this.vinculoOrganizadorRepository = vinculoOrganizadorRepository;
        this.registrarFrequenciaPort = registrarFrequenciaPort;
        this.clock = clock;
    }

    @Override
    public Frequencia executar(String token, Integer idResponsavel) {
        if (idResponsavel == null || idResponsavel <= 0) {
            throw new AcessoNegadoException("É necessário estar autenticado para registrar o check-in.");
        }
        if (token == null || token.isBlank()) {
            throw new RegraNegocioException("O token do QR Code é obrigatório.");
        }

        CheckinQrTokenSignerPort.DadosToken dadosToken = tokenSigner.validar(token);
        Inscricao inscricao = inscricaoRepository.buscarPorId(dadosToken.getIdInscricao())
                .orElseThrow(() -> new RegraNegocioException("Inscrição do QR Code não encontrada."));
        if (!inscricao.estaAtiva()) {
            throw new RegraNegocioException("Não é possível registrar presença em uma inscrição cancelada.");
        }

        Atividade atividade = atividadeRepository.buscarPorId(inscricao.getIdAtividade())
                .orElseThrow(() -> new RegraNegocioException("Atividade da inscrição não encontrada."));
        if (atividade.getTipoFrequencia() != TipoFrequenciaEnum.CHECKIN) {
            throw new RegraNegocioException("A atividade não está configurada para check-in por QR Code.");
        }

        autorizarResponsavel(idResponsavel, atividade);

        int idInscricao;
        try {
            idInscricao = Math.toIntExact(inscricao.getId());
        } catch (ArithmeticException e) {
            throw new RegraNegocioException(
                    "O id da inscrição excede o limite aceito pelo modelo de frequência."
            );
        }

        boolean checkinJaRegistrado = frequenciaRepository
                .listarPorInscricao(idInscricao)
                .stream()
                .anyMatch(Frequencia::isPresente);
        if (checkinJaRegistrado) {
            throw new RegraNegocioException("O check-in desta inscrição já foi registrado.");
        }

        return registrarFrequenciaPort.executar(
                new ComandoRegistrarFrequencia(
                        LocalDateTime.ofInstant(clock.instant(), clock.getZone()),
                        "QR_CODE",
                        idResponsavel,
                        true,
                        inscricao.getId(),
                        TipoFrequenciaEnum.CHECKIN.name()
                )
        );
    }

    private void autorizarResponsavel(Integer idResponsavel, Atividade atividade) {
        Usuario responsavel = usuarioRepository.buscarPorId(idResponsavel)
                .orElseThrow(() -> new AcessoNegadoException("Usuário responsável não encontrado."));
        if (responsavel.getPerfil() == Perfil.ADMINISTRADOR) {
            return;
        }

        try {
            Integer eventoId = Math.toIntExact(atividade.getEventoId());
            boolean organizador = vinculoOrganizadorRepository
                    .buscarPorUsuarioEEvento(idResponsavel, eventoId)
                    .isPresent();
            if (organizador) {
                return;
            }
        } catch (ArithmeticException | NullPointerException ignored) {
            // A atividade sem evento válido não pode autorizar um responsável.
        }

        throw new AcessoNegadoException(
                "Somente um administrador ou organizador do evento pode registrar o check-in."
        );
    }
}
