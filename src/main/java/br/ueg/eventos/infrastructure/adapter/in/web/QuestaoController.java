package br.ueg.eventos.infrastructure.adapter.in.web;

import br.ueg.eventos.application.port.in.questao.AtualizarQuestaoPort;
import br.ueg.eventos.application.port.in.questao.AtualizarQuestaoPort.ComandoAtualizarQuestao;
import br.ueg.eventos.application.port.in.questao.BuscarQuestaoNoQuestionarioPort;
import br.ueg.eventos.application.port.in.questao.CriarQuestaoPort;
import br.ueg.eventos.application.port.in.questao.CriarQuestaoPort.ComandoCriarQuestao;
import br.ueg.eventos.application.port.in.questao.RemoverQuestaoPort;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Questao;
import br.ueg.eventos.domain.model.Questao.TipoQuestao;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.util.List;

public class QuestaoController {

    private final CriarQuestaoPort criarQuestaoPort;
    private final AtualizarQuestaoPort atualizarQuestaoPort;
    private final RemoverQuestaoPort removerQuestaoPort;
    private final BuscarQuestaoNoQuestionarioPort buscarQuestoesPorQuestionarioPort;

    public QuestaoController(
            CriarQuestaoPort criarQuestaoPort,
            AtualizarQuestaoPort atualizarQuestaoPort,
            RemoverQuestaoPort removerQuestaoPort,
            BuscarQuestaoNoQuestionarioPort buscarQuestoesPorQuestionarioPort) {
        this.criarQuestaoPort = criarQuestaoPort;
        this.atualizarQuestaoPort = atualizarQuestaoPort;
        this.removerQuestaoPort = removerQuestaoPort;
        this.buscarQuestoesPorQuestionarioPort = buscarQuestoesPorQuestionarioPort;
    }

    public void registerRoutes(Javalin app) {
        app.post("/questoes", this::criarQuestao);
        app.put("/questoes/{id}", this::atualizarQuestao);
        app.delete("/questoes/{id}", this::removerQuestao);
        app.get("/questoes/questionario/{idQuestionario}", this::buscarPorQuestionario);
    }

    static class QuestaoRequest {
        public Long idQuestionario; // Usado apenas na criação, geralmente
        public String enunciado;
        public String tipo; // Recebemos como String e convertemos para Enum
    }

    private void criarQuestao(Context ctx) {
        try {
            QuestaoRequest request = ctx.bodyAsClass(QuestaoRequest.class);
            TipoQuestao tipoEnum = TipoQuestao.valueOf(request.tipo.toUpperCase());

            ComandoCriarQuestao comando = new ComandoCriarQuestao(
                request.idQuestionario,
                request.enunciado,
                tipoEnum
            );

            Questao questaoCriada = criarQuestaoPort.executar(comando);
            ctx.status(HttpStatus.CREATED).json(questaoCriada);

        } catch (IllegalArgumentException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result("Tipo de questão inválido. Use: TEXTUAL, ESCOLHA_UNICA ou ESCALA_NUMERICA.");
        } catch (RegraNegocioException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result(e.getMessage());
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Erro interno: " + e.getMessage());
        }
    }

    private void atualizarQuestao(Context ctx) {
        try {
            Long id = Long.valueOf(ctx.pathParam("id"));
            QuestaoRequest request = ctx.bodyAsClass(QuestaoRequest.class);
            TipoQuestao tipoEnum = TipoQuestao.valueOf(request.tipo.toUpperCase());

            ComandoAtualizarQuestao comando = new ComandoAtualizarQuestao(
                id,
                request.enunciado,
                tipoEnum
            );

            Questao questaoAtualizada = atualizarQuestaoPort.executar(comando);
            ctx.status(HttpStatus.OK).json(questaoAtualizada);

        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result("ID da questão inválido.");
        } catch (IllegalArgumentException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result("Tipo de questão inválido.");
        } catch (RegraNegocioException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result(e.getMessage());
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Erro interno: " + e.getMessage());
        }
    }

    private void removerQuestao(Context ctx) {
        try {
            Long id = Long.valueOf(ctx.pathParam("id"));
            removerQuestaoPort.executar(id);
            ctx.status(HttpStatus.NO_CONTENT);
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result("ID da questão inválido.");
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Erro interno: " + e.getMessage());
        }
    }

    private void buscarPorQuestionario(Context ctx) {
        try {
            Long idQuestionario = Long.valueOf(ctx.pathParam("idQuestionario"));
            List<Questao> questoes = buscarQuestoesPorQuestionarioPort.executar(idQuestionario);
            
            ctx.status(HttpStatus.OK).json(questoes);
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result("ID do questionário inválido.");
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Erro interno: " + e.getMessage());
        }
    }
}