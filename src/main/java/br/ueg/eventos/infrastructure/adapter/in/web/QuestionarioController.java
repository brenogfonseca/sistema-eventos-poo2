package br.ueg.eventos.infrastructure.adapter.in.web;

import br.ueg.eventos.application.port.in.questionario.AtualizarQuestionarioPort;
import br.ueg.eventos.application.port.in.questionario.AtualizarQuestionarioPort.ComandoAtualizarQuestionario;
import br.ueg.eventos.application.port.in.questionario.BuscarQuestionarioPort;
import br.ueg.eventos.application.port.in.questionario.CriarQuestionarioPort;
import br.ueg.eventos.application.port.in.questionario.CriarQuestionarioPort.ComandoCriarQuestionario;
import br.ueg.eventos.application.port.in.questionario.RemoverQuestionarioPort;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Questionario;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.util.Optional;

// Controlador Web responsável por receber as requisições HTTP e orquestrar as chamadas para os Casos de Uso.
public class QuestionarioController {

    private final CriarQuestionarioPort criarQuestionarioPort;
    private final BuscarQuestionarioPort buscarQuestionarioPort;
    private final AtualizarQuestionarioPort atualizarQuestionarioPort;
    private final RemoverQuestionarioPort removerQuestionarioPort;

    // Injeção de dependências estritamente voltada para as interfaces (Ports) da Arquitetura Hexagonal.
    public QuestionarioController(
            CriarQuestionarioPort criarQuestionarioPort,
            BuscarQuestionarioPort buscarQuestionarioPort,
            AtualizarQuestionarioPort atualizarQuestionarioPort,
            RemoverQuestionarioPort removerQuestionarioPort) {
        this.criarQuestionarioPort = criarQuestionarioPort;
        this.buscarQuestionarioPort = buscarQuestionarioPort;
        this.atualizarQuestionarioPort = atualizarQuestionarioPort;
        this.removerQuestionarioPort = removerQuestionarioPort;
    }

    // Registra os endpoints da API vinculando as rotas HTTP aos seus respectivos métodos tratadores.
    public void registerRoutes(Javalin app) {
        app.post("/questionarios", this::criarQuestionario);
        app.get("/questionarios/atividade/{idAtividade}", this::buscarPorAtividade);
        app.put("/questionarios/{id}", this::atualizarQuestionario);
        app.delete("/questionarios/{id}", this::removerQuestionario);
    }

    // DTO (Data Transfer Object) interno para mapear o JSON de entrada na criação do questionário.
    static class CriarQuestionarioRequest {
        public Long idAtividade;
        public String titulo;
    }

    // DTO interno para mapear o JSON de entrada na atualização (permite apenas o título).
    static class AtualizarQuestionarioRequest {
        public String titulo;
    }

    // Processa a requisição POST para criar um novo questionário.
    private void criarQuestionario(Context ctx) {
        try {
            CriarQuestionarioRequest request = ctx.bodyAsClass(CriarQuestionarioRequest.class);

            // Traduz o DTO da web para o Comando aceito pela camada de aplicação.
            ComandoCriarQuestionario comando = new ComandoCriarQuestionario(
                request.idAtividade,
                request.titulo
            );

            Questionario questionarioCriado = criarQuestionarioPort.executar(comando);

            // Retorna o status 201 Created junto com o objeto criado no corpo da resposta.
            ctx.status(HttpStatus.CREATED).json(questionarioCriado);

        } catch (RegraNegocioException e) {
            // Captura falhas de regras de domínio (como já existir questionário) e retorna 400 Bad Request.
            ctx.status(HttpStatus.BAD_REQUEST).result(e.getMessage());
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Erro interno: " + e.getMessage());
        }
    }

    // Processa a requisição GET para buscar o questionário vinculado a um ID de atividade.
    private void buscarPorAtividade(Context ctx) {
        try {
            Long idAtividade = Long.valueOf(ctx.pathParam("idAtividade"));
            Optional<Questionario> questionario = buscarQuestionarioPort.executar(idAtividade);

            if (questionario.isPresent()) {
                // Se o questionário existir, retorna 200 OK com os dados.
                ctx.status(HttpStatus.OK).json(questionario.get());
            } else {
                // Caso contrário, retorna 404 Not Found.
                ctx.status(HttpStatus.NOT_FOUND).result("Questionário não encontrado para a atividade informada.");
            }
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result("ID da atividade inválido.");
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Erro interno: " + e.getMessage());
        }
    }

    // Processa a requisição PUT para atualizar os dados de um questionário.
    private void atualizarQuestionario(Context ctx) {
        try {
            Long id = Long.valueOf(ctx.pathParam("id"));
            AtualizarQuestionarioRequest request = ctx.bodyAsClass(AtualizarQuestionarioRequest.class);

            // Mapeia o ID da URL e o corpo da requisição para o Comando.
            ComandoAtualizarQuestionario comando = new ComandoAtualizarQuestionario(
                id,
                request.titulo
            );

            Questionario questionarioAtualizado = atualizarQuestionarioPort.executar(comando);

            // Retorna 200 OK com o objeto atualizado.
            ctx.status(HttpStatus.OK).json(questionarioAtualizado);

        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result("ID do questionário inválido.");
        } catch (RegraNegocioException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result(e.getMessage());
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Erro interno: " + e.getMessage());
        }
    }

    // Processa a requisição DELETE para apagar um questionário.
    private void removerQuestionario(Context ctx) {
        try {
            Long id = Long.valueOf(ctx.pathParam("id"));
            removerQuestionarioPort.executar(id);
            
            // Retorna 204 No Content indicando que a remoção foi concluída sem retornar um corpo de resposta.
            ctx.status(HttpStatus.NO_CONTENT);

        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result("ID do questionário inválido.");
        } catch (RegraNegocioException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result(e.getMessage());
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Erro interno: " + e.getMessage());
        }
    }
}