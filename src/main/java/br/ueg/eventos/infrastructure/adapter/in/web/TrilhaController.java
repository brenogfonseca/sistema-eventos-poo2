package br.ueg.eventos.infrastructure.adapter.in.web;

import br.ueg.eventos.application.port.in.trilha.AlterarTrilhaPort;
import br.ueg.eventos.application.port.in.trilha.BuscarTrilhaPort;
import br.ueg.eventos.application.port.in.trilha.CadastrarTrilhaPort;
import br.ueg.eventos.application.port.in.trilha.DeletarTrilhaPort;
import br.ueg.eventos.application.port.in.trilha.ListarTrilhasPort;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Trilha;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

/** Controlador REST das trilhas. */
public class TrilhaController {

    private final CadastrarTrilhaPort cadastrarTrilhaPort;
    private final ListarTrilhasPort listarTrilhasPort;
    private final BuscarTrilhaPort buscarTrilhaPort;
    private final AlterarTrilhaPort alterarTrilhaPort;
    private final DeletarTrilhaPort deletarTrilhaPort;

    public TrilhaController(
            CadastrarTrilhaPort cadastrarTrilhaPort,
            ListarTrilhasPort listarTrilhasPort,
            BuscarTrilhaPort buscarTrilhaPort,
            AlterarTrilhaPort alterarTrilhaPort,
            DeletarTrilhaPort deletarTrilhaPort) {
        this.cadastrarTrilhaPort = cadastrarTrilhaPort;
        this.listarTrilhasPort = listarTrilhasPort;
        this.buscarTrilhaPort = buscarTrilhaPort;
        this.alterarTrilhaPort = alterarTrilhaPort;
        this.deletarTrilhaPort = deletarTrilhaPort;
    }

    // Registra as rotas de Trilha.
    public void registerRoutes(Javalin app) {
        app.post("/trilhas", this::cadastrar);
        app.get("/trilhas", this::listar);
        app.get("/trilhas/{id}", this::buscar);
        app.put("/trilhas/{id}", this::alterar);
        app.delete("/trilhas/{id}", this::deletar);
    }

    // Dados recebidos no corpo da requisição.
    static class TrilhaRequest {
        public String nome;
    }

    private void cadastrar(Context ctx) {
        try {
            TrilhaRequest request = ctx.bodyAsClass(TrilhaRequest.class);
            Trilha trilha = cadastrarTrilhaPort.executar(
                    new CadastrarTrilhaPort.ComandoCadastrarTrilha(request.nome)
            );
            ctx.status(HttpStatus.CREATED).json(trilha);
        } catch (RegraNegocioException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result(e.getMessage());
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .result("Erro interno: " + e.getMessage());
        }
    }

    private void listar(Context ctx) {
        try {
            ctx.status(HttpStatus.OK).json(listarTrilhasPort.executar());
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .result("Erro interno: " + e.getMessage());
        }
    }

    private void buscar(Context ctx) {
        try {
            Long id = Long.parseLong(ctx.pathParam("id"));
            ctx.status(HttpStatus.OK).json(buscarTrilhaPort.executar(id));
        } catch (RegraNegocioException e) {
            ctx.status(HttpStatus.NOT_FOUND).result(e.getMessage());
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result("ID inválido");
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .result("Erro interno: " + e.getMessage());
        }
    }

    private void alterar(Context ctx) {
        try {
            Long id = Long.parseLong(ctx.pathParam("id"));
            TrilhaRequest request = ctx.bodyAsClass(TrilhaRequest.class);
            Trilha trilha = alterarTrilhaPort.executar(
                    new AlterarTrilhaPort.ComandoAlterarTrilha(id, request.nome)
            );
            ctx.status(HttpStatus.OK).json(trilha);
        } catch (RegraNegocioException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result(e.getMessage());
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result("ID inválido");
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .result("Erro interno: " + e.getMessage());
        }
    }

    private void deletar(Context ctx) {
        try {
            Long id = Long.parseLong(ctx.pathParam("id"));
            deletarTrilhaPort.executar(id);
            ctx.status(HttpStatus.NO_CONTENT);
        } catch (RegraNegocioException e) {
            ctx.status(HttpStatus.NOT_FOUND).result(e.getMessage());
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result("ID inválido");
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .result("Erro interno: " + e.getMessage());
        }
    }
}