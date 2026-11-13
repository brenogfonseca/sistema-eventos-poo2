package br.ueg.eventos.application.service.frequencia;

import br.ueg.eventos.application.port.in.frequencia.RegistrarFrequenciaPort;
import br.ueg.eventos.application.port.in.frequencia.RegistrarPresencaManualPort;
import br.ueg.eventos.application.port.in.frequencia.RegistrarFrequenciaPort.ComandoRegistrarFrequencia;
import br.ueg.eventos.application.port.out.AtividadeRepositoryPort;
import br.ueg.eventos.application.port.out.InscricaoEventoRepositoryPort;
import br.ueg.eventos.application.port.out.InscricaoRepositoryPort;
import br.ueg.eventos.application.port.out.UsuarioRepositoryPort;
import br.ueg.eventos.application.port.out.VinculoOrganizadorRepositoryPort;
import br.ueg.eventos.domain.exception.AcessoNegadoException;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Atividade;
import br.ueg.eventos.domain.model.InscricaoEvento;
import br.ueg.eventos.domain.model.Perfil;
import br.ueg.eventos.domain.model.TipoFrequenciaEnum;
import br.ueg.eventos.domain.model.Usuario;

import java.time.Clock;
import java.time.LocalDateTime;

/** Marca manualmente a presença com responsável e horário para auditoria. */
public class RegistrarPresencaManualService implements RegistrarPresencaManualPort {
    private final InscricaoEventoRepositoryPort inscricaoEventoRepository;
    private final InscricaoRepositoryPort inscricaoRepository;
    private final AtividadeRepositoryPort atividadeRepository;
    private final UsuarioRepositoryPort usuarioRepository;
    private final VinculoOrganizadorRepositoryPort vinculoOrganizadorRepository;
    private final RegistrarFrequenciaPort registrarFrequenciaPort;
    private final Clock clock;

    public RegistrarPresencaManualService(InscricaoEventoRepositoryPort inscricaoEventoRepository,
                                          InscricaoRepositoryPort inscricaoRepository,
                                          AtividadeRepositoryPort atividadeRepository,
                                          UsuarioRepositoryPort usuarioRepository,
                                          VinculoOrganizadorRepositoryPort vinculoOrganizadorRepository,
                                          RegistrarFrequenciaPort registrarFrequenciaPort) {
        this(inscricaoEventoRepository, inscricaoRepository, atividadeRepository, usuarioRepository,
                vinculoOrganizadorRepository, registrarFrequenciaPort, Clock.systemDefaultZone());
    }

    RegistrarPresencaManualService(InscricaoEventoRepositoryPort inscricaoEventoRepository,
                                   InscricaoRepositoryPort inscricaoRepository,
                                   AtividadeRepositoryPort atividadeRepository,
                                   UsuarioRepositoryPort usuarioRepository,
                                   VinculoOrganizadorRepositoryPort vinculoOrganizadorRepository,
                                   RegistrarFrequenciaPort registrarFrequenciaPort, Clock clock) {
        this.inscricaoEventoRepository = inscricaoEventoRepository;
        this.inscricaoRepository = inscricaoRepository;
        this.atividadeRepository = atividadeRepository;
        this.usuarioRepository = usuarioRepository;
        this.vinculoOrganizadorRepository = vinculoOrganizadorRepository;
        this.registrarFrequenciaPort = registrarFrequenciaPort;
        this.clock = clock;
    }

    @Override
    public br.ueg.eventos.domain.model.Frequencia executar(Long idInscricaoEvento, Long idAtividade,
                                                           boolean presente, Integer idResponsavel) {
        if (idResponsavel == null || idResponsavel <= 0) {
            throw new AcessoNegadoException("É necessário autenticar o organizador.");
        }
        InscricaoEvento inscricaoEvento = inscricaoEventoRepository.buscarPorId(idInscricaoEvento)
                .orElseThrow(() -> new RegraNegocioException("Inscrição no evento não encontrada."));
        if (!inscricaoEvento.estaAtiva()) throw new RegraNegocioException("A inscrição no evento está cancelada.");
        Atividade atividade = atividadeRepository.buscarPorId(idAtividade)
                .orElseThrow(() -> new RegraNegocioException("Atividade não encontrada."));
        if (!atividade.getEventoId().equals(inscricaoEvento.getIdEvento())) {
            throw new RegraNegocioException("A atividade não pertence ao evento informado.");
        }
        if (atividade.getTipoFrequencia() != TipoFrequenciaEnum.MANUAL) {
            throw new RegraNegocioException("Esta atividade não está configurada para validação manual.");
        }
        var inscricaoAtividade = inscricaoRepository
                .buscarAtivaPorInscricaoEventoEAtividade(idInscricaoEvento, idAtividade)
                .orElseThrow(() -> new RegraNegocioException("O participante não escolheu esta atividade."));
        autorizar(idResponsavel, atividade);
        return registrarFrequenciaPort.executar(new ComandoRegistrarFrequencia(
                LocalDateTime.ofInstant(clock.instant(), clock.getZone()),
                "MANUAL",
                idResponsavel,
                presente,
                inscricaoAtividade.getId(),
                TipoFrequenciaEnum.MANUAL.name()
        ));
    }

    private void autorizar(Integer idResponsavel, Atividade atividade) {
        Usuario usuario = usuarioRepository.buscarPorId(idResponsavel)
                .orElseThrow(() -> new AcessoNegadoException("Usuário organizador não encontrado."));
        if (usuario.getPerfil() == Perfil.ADMINISTRADOR) return;
        try {
            if (vinculoOrganizadorRepository.buscarPorUsuarioEEvento(
                    idResponsavel, Math.toIntExact(atividade.getEventoId())).isPresent()) return;
        } catch (ArithmeticException ignored) {
            // Id inválido não concede acesso.
        }
        throw new AcessoNegadoException("Somente um administrador ou organizador do evento pode validar a presença.");
    }
}
