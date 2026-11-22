package br.ueg.eventos.infrastructure.adapter.in.web;

import br.ueg.eventos.application.port.out.AtividadeRepositoryPort;
import br.ueg.eventos.application.port.out.EventoRepositoryPort;
import br.ueg.eventos.application.port.out.QuestaoRepositoryPort;
import br.ueg.eventos.application.port.out.QuestionarioRepositoryPort;
import br.ueg.eventos.application.port.out.UsuarioRepositoryPort;
import br.ueg.eventos.application.port.out.VinculoOrganizadorRepositoryPort;
import br.ueg.eventos.domain.exception.AcessoNegadoException;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Perfil;
import io.javalin.http.Context;

/** Server-side checks for actions scoped to an event and its activities. */
public final class EventoAuthorization {
    private final AtividadeRepositoryPort atividades;
    private final EventoRepositoryPort eventos;
    private final UsuarioRepositoryPort usuarios;
    private final VinculoOrganizadorRepositoryPort organizadores;
    private final QuestionarioRepositoryPort questionarios;
    private final QuestaoRepositoryPort questoes;

    public EventoAuthorization(AtividadeRepositoryPort atividades,
                               EventoRepositoryPort eventos,
                               UsuarioRepositoryPort usuarios,
                               VinculoOrganizadorRepositoryPort organizadores,
                               QuestionarioRepositoryPort questionarios,
                               QuestaoRepositoryPort questoes) {
        this.atividades = atividades;
        this.eventos = eventos;
        this.usuarios = usuarios;
        this.organizadores = organizadores;
        this.questionarios = questionarios;
        this.questoes = questoes;
    }

    /** Compatibilidade para controladores antigos sem leitura de programação de rascunhos. */
    public EventoAuthorization(AtividadeRepositoryPort atividades,
                               UsuarioRepositoryPort usuarios,
                               VinculoOrganizadorRepositoryPort organizadores,
                               QuestionarioRepositoryPort questionarios,
                               QuestaoRepositoryPort questoes) {
        this(atividades, null, usuarios, organizadores, questionarios, questoes);
    }

    public int usuarioAutenticado(Context ctx) {
        Integer id = ctx.attribute(BearerAuthenticationFilter.ATTRIBUTE_USER_ID);
        if (id == null || id <= 0) throw new AcessoNegadoException("Sessão autenticada obrigatória.");
        return id;
    }

    public void exigirAdministrador(Context ctx) {
        if (!ehAdministrador(ctx)) {
            throw new AcessoNegadoException("Esta operação exige perfil de administrador.");
        }
    }

    public boolean ehAdministrador(Context ctx) {
        var usuario = usuarios.buscarPorId(usuarioAutenticado(ctx))
                .orElseThrow(() -> new AcessoNegadoException("Usuário autenticado não encontrado."));
        return usuario.getPerfil() == Perfil.ADMINISTRADOR;
    }

    public void exigirGerenciadorDaAtividade(Context ctx, Long idAtividade) {
        if (idAtividade == null || idAtividade <= 0) throw new RegraNegocioException("Atividade inválida.");
        var atividade = atividades.buscarPorId(idAtividade)
                .orElseThrow(() -> new RegraNegocioException("Atividade não encontrada."));
        exigirGerenciadorDoEvento(ctx, atividade.getEventoId());
    }

    /** Publica a programação sem autenticação; rascunhos ficam restritos aos responsáveis. */
    public void exigirLeituraDaProgramacao(Context ctx, Long idEvento) {
        if (eventos == null) {
            exigirGerenciadorDoEvento(ctx, idEvento);
            return;
        }
        var evento = eventos.buscarPorId(idEvento)
                .orElseThrow(() -> new RegraNegocioException("Evento não encontrado."));
        if (evento.isDeletado()) {
            throw new RegraNegocioException("Evento não encontrado.");
        }
        if (!evento.isPublicado()) {
            exigirGerenciadorDoEvento(ctx, idEvento);
        }
    }

    public void exigirLeituraDaAtividade(Context ctx, Long idAtividade) {
        if (idAtividade == null || idAtividade <= 0) {
            throw new RegraNegocioException("Atividade inválida.");
        }
        var atividade = atividades.buscarPorId(idAtividade)
                .orElseThrow(() -> new RegraNegocioException("Atividade não encontrada."));
        exigirLeituraDaProgramacao(ctx, atividade.getEventoId());
    }

    public void exigirGerenciadorDoQuestionario(Context ctx, Long idQuestionario) {
        var questionario = questionarios.buscarPorId(idQuestionario)
                .orElseThrow(() -> new RegraNegocioException("Questionário não encontrado."));
        exigirGerenciadorDaAtividade(ctx, questionario.getIdAtividade().longValue());
    }

    public void exigirLeituraDoQuestionario(Context ctx, Long idQuestionario) {
        var questionario = questionarios.buscarPorId(idQuestionario)
                .orElseThrow(() -> new RegraNegocioException("Questionário não encontrado."));
        if (!questionario.isPublicado()) {
            exigirGerenciadorDaAtividade(ctx, questionario.getIdAtividade().longValue());
        }
    }

    public void exigirGerenciadorDaQuestao(Context ctx, Long idQuestao) {
        var questao = questoes.buscarPorId(idQuestao)
                .orElseThrow(() -> new RegraNegocioException("Questão não encontrada."));
        var questionario = questionarios.buscarPorId(questao.getIdQuestionario().longValue())
                .orElseThrow(() -> new RegraNegocioException("Questionário não encontrado."));
        exigirGerenciadorDaAtividade(ctx, questionario.getIdAtividade().longValue());
    }

    public void exigirGerenciadorDoEvento(Context ctx, Long idEvento) {
        int idUsuario = usuarioAutenticado(ctx);
        var usuario = usuarios.buscarPorId(idUsuario)
                .orElseThrow(() -> new AcessoNegadoException("Usuário autenticado não encontrado."));
        if (usuario.getPerfil() == Perfil.ADMINISTRADOR) return;
        try {
            if (organizadores.buscarPorUsuarioEEvento(idUsuario, Math.toIntExact(idEvento)).isPresent()) return;
        } catch (ArithmeticException ignored) {
            // Um ID fora do limite do modelo de organização não concede acesso.
        }
        throw new AcessoNegadoException("Somente o organizador do evento pode realizar esta operação.");
    }
}
