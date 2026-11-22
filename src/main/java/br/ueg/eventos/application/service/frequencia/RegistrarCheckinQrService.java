package br.ueg.eventos.application.service.frequencia;

import br.ueg.eventos.application.port.in.frequencia.RegistrarCheckinQrPort;
import br.ueg.eventos.application.port.in.frequencia.RegistrarCheckinPorInscricaoPort;
import br.ueg.eventos.application.port.in.frequencia.RegistrarFrequenciaPort;
import br.ueg.eventos.application.port.in.frequencia.RegistrarFrequenciaPort.ComandoRegistrarFrequencia;
import br.ueg.eventos.application.port.out.AtividadeRepositoryPort;
import br.ueg.eventos.application.port.out.CheckinQrTokenSignerPort;
import br.ueg.eventos.application.port.out.FrequenciaRepositoryPort;
import br.ueg.eventos.application.port.out.InscricaoEventoRepositoryPort;
import br.ueg.eventos.application.port.out.InscricaoRepositoryPort;
import br.ueg.eventos.application.port.out.UsuarioRepositoryPort;
import br.ueg.eventos.application.port.out.VinculoOrganizadorRepositoryPort;
import br.ueg.eventos.application.port.out.VinculoColaboradorRepositoryPort;
import br.ueg.eventos.domain.exception.AcessoNegadoException;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Atividade;
import br.ueg.eventos.domain.model.Frequencia;
import br.ueg.eventos.domain.model.Inscricao;
import br.ueg.eventos.domain.model.InscricaoEvento;
import br.ueg.eventos.domain.model.Perfil;
import br.ueg.eventos.domain.model.TipoFrequenciaEnum;
import br.ueg.eventos.domain.model.MetodoIdentificacaoCheckinEnum;
import br.ueg.eventos.domain.model.Usuario;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

/** Valida o QR único do evento e registra presença na atividade escolhida pelo organizador. */
public class RegistrarCheckinQrService implements RegistrarCheckinQrPort, RegistrarCheckinPorInscricaoPort {
    private final CheckinQrTokenSignerPort tokenSigner;
    private final InscricaoEventoRepositoryPort inscricaoEventoRepository;
    private final InscricaoRepositoryPort inscricaoRepository;
    private final AtividadeRepositoryPort atividadeRepository;
    private final FrequenciaRepositoryPort frequenciaRepository;
    private final UsuarioRepositoryPort usuarioRepository;
    private final VinculoOrganizadorRepositoryPort vinculoOrganizadorRepository;
    private final VinculoColaboradorRepositoryPort vinculoColaboradorRepository;
    private final RegistrarFrequenciaPort registrarFrequenciaPort;
    private final Clock clock;

    public RegistrarCheckinQrService(CheckinQrTokenSignerPort tokenSigner,
                                     InscricaoEventoRepositoryPort inscricaoEventoRepository,
                                     InscricaoRepositoryPort inscricaoRepository,
                                     AtividadeRepositoryPort atividadeRepository,
                                     FrequenciaRepositoryPort frequenciaRepository,
                                     UsuarioRepositoryPort usuarioRepository,
                                     VinculoOrganizadorRepositoryPort vinculoOrganizadorRepository,
                                     VinculoColaboradorRepositoryPort vinculoColaboradorRepository,
                                     RegistrarFrequenciaPort registrarFrequenciaPort) {
        this(tokenSigner, inscricaoEventoRepository, inscricaoRepository, atividadeRepository,
                frequenciaRepository, usuarioRepository, vinculoOrganizadorRepository,
                vinculoColaboradorRepository, registrarFrequenciaPort, Clock.systemUTC());
    }

    RegistrarCheckinQrService(CheckinQrTokenSignerPort tokenSigner,
                              InscricaoEventoRepositoryPort inscricaoEventoRepository,
                              InscricaoRepositoryPort inscricaoRepository,
                              AtividadeRepositoryPort atividadeRepository,
                              FrequenciaRepositoryPort frequenciaRepository,
                              UsuarioRepositoryPort usuarioRepository,
                              VinculoOrganizadorRepositoryPort vinculoOrganizadorRepository,
                              VinculoColaboradorRepositoryPort vinculoColaboradorRepository,
                              RegistrarFrequenciaPort registrarFrequenciaPort,
                              Clock clock) {
        this.tokenSigner = tokenSigner;
        this.inscricaoEventoRepository = inscricaoEventoRepository;
        this.inscricaoRepository = inscricaoRepository;
        this.atividadeRepository = atividadeRepository;
        this.frequenciaRepository = frequenciaRepository;
        this.usuarioRepository = usuarioRepository;
        this.vinculoOrganizadorRepository = vinculoOrganizadorRepository;
        this.vinculoColaboradorRepository = vinculoColaboradorRepository;
        this.registrarFrequenciaPort = registrarFrequenciaPort;
        this.clock = clock;
    }

    @Override
    public Frequencia executar(String token, Long idAtividade, String acao, Integer idResponsavel) {
        CheckinQrTokenSignerPort.DadosToken dados = tokenSigner.validar(token);
        return registrar(dados.getIdInscricaoEvento(), idAtividade, acao, idResponsavel,
                MetodoIdentificacaoCheckinEnum.QR);
    }

    @Override
    public Frequencia executar(Long idInscricaoEvento, Long idAtividade, String acao, Integer idResponsavel) {
        return registrar(idInscricaoEvento, idAtividade, acao, idResponsavel,
                MetodoIdentificacaoCheckinEnum.NOME_CPF);
    }

    private Frequencia registrar(Long idInscricaoEvento, Long idAtividade, String acao, Integer idResponsavel,
                                 MetodoIdentificacaoCheckinEnum metodoIdentificacao) {
        if (idResponsavel == null || idResponsavel <= 0) {
            throw new AcessoNegadoException("É necessário autenticar a equipe do evento para registrar a presença.");
        }
        if (idAtividade == null || idAtividade <= 0) {
            throw new RegraNegocioException("Selecione uma atividade válida.");
        }
        InscricaoEvento inscricaoEvento = inscricaoEventoRepository.buscarPorId(idInscricaoEvento)
                .orElseThrow(() -> new RegraNegocioException("Inscrição no evento do QR Code não encontrada."));
        if (!inscricaoEvento.estaAtiva()) {
            throw new RegraNegocioException("A inscrição no evento está cancelada.");
        }

        Atividade atividade = atividadeRepository.buscarPorId(idAtividade)
                .orElseThrow(() -> new RegraNegocioException("Atividade não encontrada."));
        if (!atividade.getEventoId().equals(inscricaoEvento.getIdEvento())) {
            throw new RegraNegocioException("A atividade não pertence ao evento deste QR Code.");
        }
        if (atividade.getMetodoIdentificacaoCheckin() != metodoIdentificacao) {
            throw new RegraNegocioException(metodoIdentificacao == MetodoIdentificacaoCheckinEnum.QR
                    ? "Esta atividade exige identificação por nome ou CPF."
                    : "Esta atividade exige leitura de QR Code.");
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
            if (!"PRESENTE".equals(acaoNormalizada)) {
                throw new RegraNegocioException("A equipe de portaria só pode registrar presença. Somente o organizador pode marcar ausência.");
            }
            if (marcacoes.stream().anyMatch(f -> "MANUAL".equalsIgnoreCase(f.getTipo())
                    && List.of("QR_MANUAL", "BUSCA_MANUAL").contains(f.getOrigem().toUpperCase()))) {
                throw new RegraNegocioException("A presença desta atividade já foi registrada.");
            }
            origem = metodoIdentificacao == MetodoIdentificacaoCheckinEnum.QR ? "QR_MANUAL" : "BUSCA_MANUAL";
            return registrarFrequenciaPort.executar(new ComandoRegistrarFrequencia(
                    Instant.now(clock),
                    origem, idResponsavel, presente, inscricaoAtividade.getId(), politica.name()));
        }
        if (politica == TipoFrequenciaEnum.CHECKIN) {
            if (!acaoNormalizada.isEmpty() && !"CHECKIN".equals(acaoNormalizada)) {
                throw new RegraNegocioException("Para esta atividade, a ação deve ser CHECKIN.");
            }
            if (marcacoes.stream().anyMatch(f -> f.isPresente() && "CHECKIN".equalsIgnoreCase(f.getTipo())
                    && List.of("QR_CHECKIN", "BUSCA_CHECKIN").contains(f.getOrigem().toUpperCase()))) {
                throw new RegraNegocioException("O check-in desta atividade já foi registrado.");
            }
            origem = metodoIdentificacao == MetodoIdentificacaoCheckinEnum.QR ? "QR_CHECKIN" : "BUSCA_CHECKIN";
        } else {
            if (!"ENTRADA".equals(acaoNormalizada) && !"SAIDA".equals(acaoNormalizada)) {
                throw new RegraNegocioException("Escolha ENTRADA ou SAIDA para esta atividade.");
            }
            boolean entrada = marcacoes.stream().anyMatch(f -> List.of("QR_ENTRADA", "BUSCA_ENTRADA").contains(f.getOrigem().toUpperCase()));
            boolean saida = marcacoes.stream().anyMatch(f -> List.of("QR_SAIDA", "BUSCA_SAIDA").contains(f.getOrigem().toUpperCase()));
            if ("ENTRADA".equals(acaoNormalizada)) {
                if (entrada) throw new RegraNegocioException("A entrada já foi registrada.");
                if (saida) throw new RegraNegocioException("Não é possível registrar entrada depois da saída.");
                origem = metodoIdentificacao == MetodoIdentificacaoCheckinEnum.QR ? "QR_ENTRADA" : "BUSCA_ENTRADA";
            } else {
                if (!entrada) throw new RegraNegocioException("Registre a entrada antes da saída.");
                if (saida) throw new RegraNegocioException("A saída já foi registrada.");
                origem = metodoIdentificacao == MetodoIdentificacaoCheckinEnum.QR ? "QR_SAIDA" : "BUSCA_SAIDA";
            }
        }

        return registrarFrequenciaPort.executar(new ComandoRegistrarFrequencia(
                Instant.now(clock),
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
        if (atividade.getId() != null
                && vinculoColaboradorRepository.estaVinculado(idResponsavel, atividade.getId())) return;
        throw new AcessoNegadoException("Somente o organizador ou colaborador vinculado à atividade pode registrar a presença.");
    }
}
