package br.ueg.eventos.application.service.frequencia;

import br.ueg.eventos.application.port.in.frequencia.RegistrarCheckinQrPort;
import br.ueg.eventos.application.port.in.frequencia.RegistrarFrequenciaPort;
import br.ueg.eventos.application.port.in.frequencia.RegistrarFrequenciaPort.ComandoRegistrarFrequencia;
import br.ueg.eventos.application.port.out.AtividadeRepositoryPort;
import br.ueg.eventos.application.port.out.CheckinQrTokenSignerPort;
import br.ueg.eventos.application.port.out.FrequenciaRepositoryPort;
import br.ueg.eventos.application.port.out.InscricaoEventoRepositoryPort;
import br.ueg.eventos.application.port.out.InscricaoRepositoryPort;
import br.ueg.eventos.application.port.out.UsuarioRepositoryPort;
import br.ueg.eventos.application.port.out.VinculoOrganizadorRepositoryPort;
import br.ueg.eventos.domain.exception.AcessoNegadoException;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Atividade;
import br.ueg.eventos.domain.model.Frequencia;
import br.ueg.eventos.domain.model.Inscricao;
import br.ueg.eventos.domain.model.InscricaoEvento;
import br.ueg.eventos.domain.model.Perfil;
import br.ueg.eventos.domain.model.TipoFrequenciaEnum;
import br.ueg.eventos.domain.model.Usuario;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

/** Valida o QR único do evento e registra presença na atividade escolhida pelo organizador. */
public class RegistrarCheckinQrService implements RegistrarCheckinQrPort {
    private final CheckinQrTokenSignerPort tokenSigner;
    private final InscricaoEventoRepositoryPort inscricaoEventoRepository;
    private final InscricaoRepositoryPort inscricaoRepository;
    private final AtividadeRepositoryPort atividadeRepository;
    private final FrequenciaRepositoryPort frequenciaRepository;
    private final UsuarioRepositoryPort usuarioRepository;
    private final VinculoOrganizadorRepositoryPort vinculoOrganizadorRepository;
    private final RegistrarFrequenciaPort registrarFrequenciaPort;
    private final Clock clock;

    public RegistrarCheckinQrService(CheckinQrTokenSignerPort tokenSigner,
                                     InscricaoEventoRepositoryPort inscricaoEventoRepository,
                                     InscricaoRepositoryPort inscricaoRepository,
                                     AtividadeRepositoryPort atividadeRepository,
                                     FrequenciaRepositoryPort frequenciaRepository,
                                     UsuarioRepositoryPort usuarioRepository,
                                     VinculoOrganizadorRepositoryPort vinculoOrganizadorRepository,
                                     RegistrarFrequenciaPort registrarFrequenciaPort) {
        this(tokenSigner, inscricaoEventoRepository, inscricaoRepository, atividadeRepository,
                frequenciaRepository, usuarioRepository, vinculoOrganizadorRepository,
                registrarFrequenciaPort, Clock.systemDefaultZone());
    }

    RegistrarCheckinQrService(CheckinQrTokenSignerPort tokenSigner,
                              InscricaoEventoRepositoryPort inscricaoEventoRepository,
                              InscricaoRepositoryPort inscricaoRepository,
                              AtividadeRepositoryPort atividadeRepository,
                              FrequenciaRepositoryPort frequenciaRepository,
                              UsuarioRepositoryPort usuarioRepository,
                              VinculoOrganizadorRepositoryPort vinculoOrganizadorRepository,
                              RegistrarFrequenciaPort registrarFrequenciaPort,
                              Clock clock) {
        this.tokenSigner = tokenSigner;
        this.inscricaoEventoRepository = inscricaoEventoRepository;
        this.inscricaoRepository = inscricaoRepository;
        this.atividadeRepository = atividadeRepository;
        this.frequenciaRepository = frequenciaRepository;
        this.usuarioRepository = usuarioRepository;
        this.vinculoOrganizadorRepository = vinculoOrganizadorRepository;
        this.registrarFrequenciaPort = registrarFrequenciaPort;
        this.clock = clock;
    }

    @Override
    public Frequencia executar(String token, Long idAtividade, String acao, Integer idResponsavel) {
        if (idResponsavel == null || idResponsavel <= 0) {
            throw new AcessoNegadoException("É necessário autenticar o organizador para registrar a presença.");
        }
        if (idAtividade == null || idAtividade <= 0) {
            throw new RegraNegocioException("Selecione uma atividade válida.");
        }
        CheckinQrTokenSignerPort.DadosToken dados = tokenSigner.validar(token);
        InscricaoEvento inscricaoEvento = inscricaoEventoRepository.buscarPorId(dados.getIdInscricaoEvento())
                .orElseThrow(() -> new RegraNegocioException("Inscrição no evento do QR Code não encontrada."));
        if (!inscricaoEvento.estaAtiva()) {
            throw new RegraNegocioException("A inscrição no evento está cancelada.");
        }

        Atividade atividade = atividadeRepository.buscarPorId(idAtividade)
                .orElseThrow(() -> new RegraNegocioException("Atividade não encontrada."));
        if (!atividade.getEventoId().equals(inscricaoEvento.getIdEvento())) {
            throw new RegraNegocioException("A atividade não pertence ao evento deste QR Code.");
        }
        Inscricao inscricaoAtividade = inscricaoRepository
                .buscarAtivaPorInscricaoEventoEAtividade(inscricaoEvento.getId(), idAtividade)
                .orElseThrow(() -> new RegraNegocioException("O participante não escolheu esta atividade."));

        autorizarResponsavel(idResponsavel, atividade);
        TipoFrequenciaEnum politica = atividade.getTipoFrequencia();
        if (politica == null) {
            throw new RegraNegocioException("A atividade não possui um critério de frequência configurado.");
        }

        String acaoNormalizada = acao == null ? "" : acao.trim().toUpperCase();
        List<Frequencia> marcacoes = frequenciaRepository.listarPorInscricao(
                converterId(inscricaoAtividade.getId()));
        String origem;
        boolean presente = true;
        if (politica == TipoFrequenciaEnum.MANUAL) {
            if (!"PRESENTE".equals(acaoNormalizada) && !"AUSENTE".equals(acaoNormalizada)) {
                throw new RegraNegocioException("Escolha PRESENTE ou AUSENTE para validar manualmente esta atividade.");
            }
            origem = "MANUAL";
            presente = "PRESENTE".equals(acaoNormalizada);
            return registrarFrequenciaPort.executar(new ComandoRegistrarFrequencia(
                    LocalDateTime.ofInstant(clock.instant(), clock.getZone()),
                    origem, idResponsavel, presente, inscricaoAtividade.getId(), politica.name()));
        }
        if (politica == TipoFrequenciaEnum.CHECKIN) {
            if (!acaoNormalizada.isEmpty() && !"CHECKIN".equals(acaoNormalizada)) {
                throw new RegraNegocioException("Para esta atividade, a ação deve ser CHECKIN.");
            }
            if (marcacoes.stream().anyMatch(f -> f.isPresente() && "CHECKIN".equalsIgnoreCase(f.getTipo()))) {
                throw new RegraNegocioException("O check-in desta atividade já foi registrado.");
            }
            origem = "QR_CHECKIN";
        } else {
            if (!"ENTRADA".equals(acaoNormalizada) && !"SAIDA".equals(acaoNormalizada)) {
                throw new RegraNegocioException("Escolha ENTRADA ou SAIDA para esta atividade.");
            }
            boolean entrada = marcacoes.stream().anyMatch(f -> "QR_ENTRADA".equalsIgnoreCase(f.getOrigem()));
            boolean saida = marcacoes.stream().anyMatch(f -> "QR_SAIDA".equalsIgnoreCase(f.getOrigem()));
            if ("ENTRADA".equals(acaoNormalizada)) {
                if (entrada) throw new RegraNegocioException("A entrada já foi registrada.");
                if (saida) throw new RegraNegocioException("Não é possível registrar entrada depois da saída.");
                origem = "QR_ENTRADA";
            } else {
                if (!entrada) throw new RegraNegocioException("Registre a entrada antes da saída.");
                if (saida) throw new RegraNegocioException("A saída já foi registrada.");
                origem = "QR_SAIDA";
            }
        }

        return registrarFrequenciaPort.executar(new ComandoRegistrarFrequencia(
                LocalDateTime.ofInstant(clock.instant(), clock.getZone()),
                origem,
                idResponsavel,
                presente,
                inscricaoAtividade.getId(),
                politica.name()
        ));
    }

    private int converterId(Long id) {
        try {
            return Math.toIntExact(id);
        } catch (ArithmeticException e) {
            throw new RegraNegocioException("O id da inscrição excede o limite aceito pelo sistema de frequência.");
        }
    }

    private void autorizarResponsavel(Integer idResponsavel, Atividade atividade) {
        Usuario usuario = usuarioRepository.buscarPorId(idResponsavel)
                .orElseThrow(() -> new AcessoNegadoException("Usuário organizador não encontrado."));
        if (usuario.getPerfil() == Perfil.ADMINISTRADOR) return;
        try {
            if (vinculoOrganizadorRepository.buscarPorUsuarioEEvento(
                    idResponsavel, Math.toIntExact(atividade.getEventoId())).isPresent()) return;
        } catch (ArithmeticException ignored) {
            // Um evento sem id compatível não concede autorização.
        }
        throw new AcessoNegadoException("Somente um administrador ou organizador do evento pode validar a presença.");
    }
}
