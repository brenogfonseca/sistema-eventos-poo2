package br.ueg.eventos.application.service.atividade;

import br.ueg.eventos.application.port.in.atividade.AtualizarAtividadePort;
import br.ueg.eventos.application.port.out.AtividadeRepositoryPort;
import br.ueg.eventos.application.port.out.EventoRepositoryPort;
import br.ueg.eventos.application.port.out.UsuarioRepositoryPort;
import br.ueg.eventos.application.port.out.VinculoOrganizadorRepositoryPort;
import br.ueg.eventos.domain.exception.AcessoNegadoException;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Atividade;
import br.ueg.eventos.domain.model.Evento;
import br.ueg.eventos.domain.model.Perfil;

public class AtualizarAtividadeService
        implements AtualizarAtividadePort {

    private final AtividadeRepositoryPort atividadeRepository;
    private final EventoRepositoryPort eventoRepository;
    private final UsuarioRepositoryPort usuarioRepository;
    private final VinculoOrganizadorRepositoryPort vinculoRepository;

    public AtualizarAtividadeService(
            AtividadeRepositoryPort atividadeRepository,
            EventoRepositoryPort eventoRepository,
            UsuarioRepositoryPort usuarioRepository,
            VinculoOrganizadorRepositoryPort vinculoRepository) {
        this.atividadeRepository = atividadeRepository;
        this.eventoRepository = eventoRepository;
        this.usuarioRepository = usuarioRepository;
        this.vinculoRepository = vinculoRepository;
    }

    @Override
    public Atividade executar(Long id, Atividade atividade, Integer idExecutor) {
        if (id == null || id <= 0) {
            throw new RegraNegocioException(
                    "O id da atividade deve ser válido.");
        }

        if (atividade == null) {
            throw new RegraNegocioException(
                    "A atividade é obrigatória.");
        }

        atividadeRepository.buscarPorId(id)
                .orElseThrow(() -> new RegraNegocioException(
                        "Atividade não encontrada."));

        Evento evento = eventoRepository
                .buscarPorId(atividade.getEventoId())
                .orElseThrow(() -> new RegraNegocioException(
                        "Evento não encontrado."));

        if (evento.isDeletado()) {
            throw new RegraNegocioException(
                    "Não é possível vincular a atividade a um evento excluído.");
        }

        autorizar(idExecutor, evento.getId());

        if (atividade.getId() != null
                && !id.equals(atividade.getId())) {
            throw new RegraNegocioException(
                    "O id da atividade não corresponde ao id informado.");
        }

        return atividadeRepository.salvar(atividade);
    }

    private void autorizar(Integer idExecutor, Long idEvento) {
        if (idExecutor == null || idExecutor <= 0) throw new AcessoNegadoException("É necessário autenticar o organizador.");
        var usuario = usuarioRepository.buscarPorId(idExecutor)
                .orElseThrow(() -> new AcessoNegadoException("Usuário organizador não encontrado."));
        if (usuario.getPerfil() == Perfil.ADMINISTRADOR) return;
        try {
            if (vinculoRepository.buscarPorUsuarioEEvento(idExecutor, Math.toIntExact(idEvento)).isPresent()) return;
        } catch (ArithmeticException ignored) { }
        throw new AcessoNegadoException("Somente um administrador ou organizador do evento pode configurar atividades.");
    }
}
