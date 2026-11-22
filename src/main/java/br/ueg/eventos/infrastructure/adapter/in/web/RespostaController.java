package br.ueg.eventos.infrastructure.adapter.in.web;

import br.ueg.eventos.application.port.in.resposta.AtualizarRespostaPort;
import br.ueg.eventos.application.port.in.resposta.AtualizarRespostaPort.ComandoAtualizarResposta;
import br.ueg.eventos.application.port.in.resposta.BuscarRespostaPort;
import br.ueg.eventos.application.port.in.resposta.CriarRespostaPort;
import br.ueg.eventos.application.port.in.resposta.CriarRespostaPort.ComandoCriarResposta;
import br.ueg.eventos.application.port.in.resposta.RemoverRespostaPort;
import br.ueg.eventos.application.port.out.QuestaoRepositoryPort;
import br.ueg.eventos.application.port.out.QuestionarioRepositoryPort;
import br.ueg.eventos.domain.exception.AcessoNegadoException;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Resposta;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.util.List;

public class RespostaController {

    private final CriarRespostaPort criarRespostaPort;
    private final AtualizarRespostaPort atualizarRespostaPort;
    private final RemoverRespostaPort removerRespostaPort;
    private final BuscarRespostaPort buscarRespostasPorQuestaoPort;
    private final QuestaoRepositoryPort questoes;
    private final QuestionarioRepositoryPort questionarios;
    private final EventoAuthorization autorizacao;

    public RespostaController(
            CriarRespostaPort criarRespostaPort,
            AtualizarRespostaPort atualizarRespostaPort,
            RemoverRespostaPort removerRespostaPort,
            BuscarRespostaPort buscarRespostasPorQuestaoPort,
            QuestaoRepositoryPort questoes,
            QuestionarioRepositoryPort questionarios,
            EventoAuthorization autorizacao) {
        this.criarRespostaPort = criarRespostaPort;
        this.atualizarRespostaPort = atualizarRespostaPort;
        this.removerRespostaPort = removerRespostaPort;
        this.buscarRespostasPorQuestaoPort = buscarRespostasPorQuestaoPort;
        this.questoes = questoes;
        this.questionarios = questionarios;
        this.autorizacao = autorizacao;
    }

    public void registerRoutes(Javalin app) {
        app.post("/respostas", this::criarResposta);
        app.put("/respostas/{id}", this::atualizarResposta);
        app.delete("/respostas/{id}", this::removerResposta);
        app.get("/respostas/questao/{idQuestao}", this::buscarPorQuestao);
    }

    // DTO para Criação (recebe a questão, usuário e o valor)
    static class CriarRespostaRequest {
        public Long idQuestao;
        public String valor;
    }

    // DTO para Atualização (recebe apenas o valor, pois questão e usuário não mudam)
    static class AtualizarRespostaRequest {
        public String valor;
    }

    private void criarResposta(Context ctx) {
        try {
            CriarRespostaRequest request = ctx.bodyAsClass(CriarRespostaRequest.class);

            ComandoCriarResposta comando = new ComandoCriarResposta(
                request.idQuestao,
                autorizacao.usuarioAutenticado(ctx),
                request.valor
            );

            Resposta respostaCriada = criarRespostaPort.executar(comando);
            ctx.status(HttpStatus.CREATED).json(respostaCriada);

        } catch (AcessoNegadoException e) {
            ctx.status(HttpStatus.FORBIDDEN).result(e.getMessage());
        } catch (RegraNegocioException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result(e.getMessage());
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Erro interno: " + e.getMessage());
        }
    }

    private void atualizarResposta(Context ctx) {
        try {
            Long id = Long.valueOf(ctx.pathParam("id"));
            AtualizarRespostaRequest request = ctx.bodyAsClass(AtualizarRespostaRequest.class);

            ComandoAtualizarResposta comando = new ComandoAtualizarResposta(
                id,
                request.valor,
                autorizacao.usuarioAutenticado(ctx)
            );

            Resposta respostaAtualizada = atualizarRespostaPort.executar(comando);
            ctx.status(HttpStatus.OK).json(respostaAtualizada);

        } catch (AcessoNegadoException e) {
            ctx.status(HttpStatus.FORBIDDEN).result(e.getMessage());
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result("ID da resposta inválido.");
        } catch (RegraNegocioException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result(e.getMessage());
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Erro interno: " + e.getMessage());
        }
    }

    private void removerResposta(Context ctx) {
        try {
            Long id = Long.valueOf(ctx.pathParam("id"));
            removerRespostaPort.executar(id, autorizacao.usuarioAutenticado(ctx));
            ctx.status(HttpStatus.NO_CONTENT);
        } catch (AcessoNegadoException e) {
            ctx.status(HttpStatus.FORBIDDEN).result(e.getMessage());
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result("ID da resposta inválido.");
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Erro interno: " + e.getMessage());
        }
    }

    private void buscarPorQuestao(Context ctx) {
        try {
            Long idQuestao = Long.valueOf(ctx.pathParam("idQuestao"));
            autorizacao.exigirGerenciadorDaQuestao(ctx, idQuestao);
            List<Resposta> respostas = buscarRespostasPorQuestaoPort.executar(idQuestao);
            Questao questao = questoes.buscarPorId(idQuestao)
                    .orElseThrow(() -> new RegraNegocioException("Questão não encontrada."));
            var questionario = questionarios.buscarPorId(questao.getIdQuestionario().longValue())
                    .orElseThrow(() -> new RegraNegocioException("Questionário não encontrado."));
            if (questionario.isAnonimo()) {
                ctx.status(HttpStatus.OK).json(respostas.stream()
                        .map(r -> new RespostaAnonima(r.getId(), r.getValor())).toList());
            } else {
                ctx.status(HttpStatus.OK).json(respostas);
            }
        } catch (AcessoNegadoException e) {
            ctx.status(HttpStatus.FORBIDDEN).result(e.getMessage());
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result("ID da questão inválido.");
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Erro interno: " + e.getMessage());
        }
    }

    public record RespostaAnonima(Integer id, String valor) { }
}
