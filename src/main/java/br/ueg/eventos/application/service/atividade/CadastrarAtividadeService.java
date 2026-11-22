package br.ueg.eventos.application.service.atividade;

import br.ueg.eventos.application.port.in.atividade.CadastrarAtividadePort;
import br.ueg.eventos.application.port.out.AtividadeRepositoryPort;
import br.ueg.eventos.application.port.out.EventoRepositoryPort;
import br.ueg.eventos.application.port.out.UsuarioRepositoryPort;
import br.ueg.eventos.application.port.out.VinculoOrganizadorRepositoryPort;
import br.ueg.eventos.domain.exception.AcessoNegadoException;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Atividade;
import br.ueg.eventos.domain.model.Evento;
import br.ueg.eventos.domain.model.Perfil;

public class CadastrarAtividadeService
        implements CadastrarAtividadePort {

    private final AtividadeRepositoryPort atividadeRepository;
    private final EventoRepositoryPort eventoRepository;
    private final UsuarioRepositoryPort usuarioRepository;
    private final VinculoOrganizadorRepositoryPort vinculoRepository;

    public CadastrarAtividadeService(
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
    public Atividade executar(Atividade atividade, Integer idExecutor) {
        if (atividade == null) {
            throw new RegraNegocioException(
                    "A atividade é obrigatória.");
        }

        Evento evento = eventoRepository
                .buscarPorId(atividade.getEventoId())
                .orElseThrow(() -> new RegraNegocioException(
                        "Evento não encontrado."));

        if (evento.isDeletado()) {
            throw new RegraNegocioException(
                    "Não é possível cadastrar atividades em um evento excluído.");
        }

        autorizar(idExecutor, evento.getId());
        validarPeriodoNoEvento(atividade, evento);

        return atividadeRepository.salvar(atividade);
    }

    static void validarPeriodoNoEvento(Atividade atividade, Evento evento) {
        java.time.LocalDateTime inicio = java.time.LocalDateTime.of(atividade.getData(), atividade.getHoraInicio());
        java.time.LocalDateTime fim = java.time.LocalDateTime.of(atividade.getData(), atividade.getHoraFim());
        if (fim.isBefore(inicio)) fim = fim.plusDays(1);
        if (inicio.isBefore(evento.getPeriodo().getInicio()) || fim.isAfter(evento.getPeriodo().getFim())) {
            throw new RegraNegocioException("O horário da atividade deve estar dentro do período do evento.");
        }
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
