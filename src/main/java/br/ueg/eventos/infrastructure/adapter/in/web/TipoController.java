package br.ueg.eventos.infrastructure.adapter.in.web;

import br.ueg.eventos.application.port.in.tipo.AlterarTipoPort;
import br.ueg.eventos.application.port.in.tipo.BuscarTipoPort;
import br.ueg.eventos.application.port.in.tipo.CadastrarTipoPort;
import br.ueg.eventos.application.port.in.tipo.DeletarTipoPort;
import br.ueg.eventos.application.port.in.tipo.ListarTiposPort;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Tipo;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

/** Controlador REST dos tipos. */
public class TipoController {

    private final CadastrarTipoPort cadastrarTipoPort;
    private final ListarTiposPort listarTiposPort;
    private final BuscarTipoPort buscarTipoPort;
    private final AlterarTipoPort alterarTipoPort;
    private final DeletarTipoPort deletarTipoPort;

    public TipoController(
            CadastrarTipoPort cadastrarTipoPort,
            ListarTiposPort listarTiposPort,
            BuscarTipoPort buscarTipoPort,
            AlterarTipoPort alterarTipoPort,
            DeletarTipoPort deletarTipoPort) {
        this.cadastrarTipoPort = cadastrarTipoPort;
        this.listarTiposPort = listarTiposPort;
        this.buscarTipoPort = buscarTipoPort;
        this.alterarTipoPort = alterarTipoPort;
        this.deletarTipoPort = deletarTipoPort;
    }

    // Registra as rotas de Tipo.
    public void registerRoutes(Javalin app) {
        app.post("/tipos", this::cadastrar);
        app.get("/tipos", this::listar);
        app.get("/tipos/{id}", this::buscar);
        app.put("/tipos/{id}", this::alterar);
        app.delete("/tipos/{id}", this::deletar);
    }

    // Dados recebidos no corpo da requisição.
    static class TipoRequest {
        public String nome;
    }

    private void cadastrar(Context ctx) {
        try {
            TipoRequest request = ctx.bodyAsClass(TipoRequest.class);
            Tipo tipo = cadastrarTipoPort.executar(
                    new CadastrarTipoPort.ComandoCadastrarTipo(request.nome)
            );
            ctx.status(HttpStatus.CREATED).json(tipo);
        } catch (RegraNegocioException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result(e.getMessage());
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .result("Erro interno: " + e.getMessage());
        }
    }

    private void listar(Context ctx) {
        try {
            ctx.status(HttpStatus.OK).json(listarTiposPort.executar());
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .result("Erro interno: " + e.getMessage());
        }
    }

    private void buscar(Context ctx) {
        try {
            Long id = Long.parseLong(ctx.pathParam("id"));
            ctx.status(HttpStatus.OK).json(buscarTipoPort.executar(id));
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
            TipoRequest request = ctx.bodyAsClass(TipoRequest.class);
            Tipo tipo = alterarTipoPort.executar(
                    new AlterarTipoPort.ComandoAlterarTipo(id, request.nome)
            );
            ctx.status(HttpStatus.OK).json(tipo);
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
            deletarTipoPort.executar(id);
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