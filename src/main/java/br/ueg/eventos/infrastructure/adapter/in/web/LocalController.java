package br.ueg.eventos.infrastructure.adapter.in.web;

import br.ueg.eventos.application.port.in.local.AlterarLocalPort;
import br.ueg.eventos.application.port.in.local.AlterarLocalPort.ComandoAlterarLocal;
import br.ueg.eventos.application.port.in.local.BuscarLocalPorIdPort;
import br.ueg.eventos.application.port.in.local.CadastrarLocalPort;
import br.ueg.eventos.application.port.in.local.CadastrarLocalPort.ComandoCadastrarLocal;
import br.ueg.eventos.application.port.in.local.DeletarLocalPort;
import br.ueg.eventos.application.port.in.local.ListarLocaisPort;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Local;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

/**
 * Controlador de API (Adapter IN) responsável por expor os endpoints REST para o domínio de Local.
 * Converte requisições HTTP em Comandos (Ports in) e respostas em JSON.
 */
public class LocalController {

    // Casos de uso injetados via construtor
    private final CadastrarLocalPort cadastrarLocalPort;
    private final ListarLocaisPort listarLocaisPort;
    private final BuscarLocalPorIdPort buscarLocalPorIdPort;
    private final AlterarLocalPort alterarLocalPort;
    private final DeletarLocalPort deletarLocalPort;

    public LocalController(
            CadastrarLocalPort cadastrarLocalPort,
            ListarLocaisPort listarLocaisPort,
            BuscarLocalPorIdPort buscarLocalPorIdPort,
            AlterarLocalPort alterarLocalPort,
            DeletarLocalPort deletarLocalPort) {
        this.cadastrarLocalPort = cadastrarLocalPort;
        this.listarLocaisPort = listarLocaisPort;
        this.buscarLocalPorIdPort = buscarLocalPorIdPort;
        this.alterarLocalPort = alterarLocalPort;
        this.deletarLocalPort = deletarLocalPort;
    }

    /**
     * Registra as rotas REST associadas a Local no servidor Javalin.
     */
    public void registerRoutes(Javalin app) {
        app.post("/locais", this::cadastrarLocal);
        app.get("/locais", this::listarLocais);
        app.get("/locais/{id}", this::buscarLocalPorId);
        app.put("/locais/{id}", this::alterarLocal);
        app.delete("/locais/{id}", this::deletarLocal);
    }

    /**
     * DTO interno para extrair os dados da requisição HTTP (Body).
     */
    static class LocalRequest {
        public String nome;
    }

    private void cadastrarLocal(Context ctx) {
        try {
            // Mapeia o body para o DTO
            LocalRequest request = ctx.bodyAsClass(LocalRequest.class);

            // Transforma no Comando esperado pela Porta de Entrada
            ComandoCadastrarLocal comando = new ComandoCadastrarLocal(request.nome);

            // Executa o caso de uso e retorna o resultado
            Local localCriado = cadastrarLocalPort.executar(comando);
            ctx.status(HttpStatus.CREATED).json(localCriado);

        } catch (RegraNegocioException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result(e.getMessage());
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Erro interno: " + e.getMessage());
        }
    }

    private void listarLocais(Context ctx) {
        try {
            ctx.status(HttpStatus.OK).json(listarLocaisPort.executar());
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Erro interno: " + e.getMessage());
        }
    }

    private void buscarLocalPorId(Context ctx) {
        try {
            Long id = Long.parseLong(ctx.pathParam("id"));
            Local local = buscarLocalPorIdPort.executar(id);
            ctx.status(HttpStatus.OK).json(local);
        } catch (RegraNegocioException e) {
            ctx.status(HttpStatus.NOT_FOUND).result(e.getMessage());
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result("ID inválido");
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Erro interno: " + e.getMessage());
        }
    }

    private void alterarLocal(Context ctx) {
        try {
            Long id = Long.parseLong(ctx.pathParam("id"));
            LocalRequest request = ctx.bodyAsClass(LocalRequest.class);

            ComandoAlterarLocal comando = new ComandoAlterarLocal(id, request.nome);
            Local localAlterado = alterarLocalPort.executar(comando);

            ctx.status(HttpStatus.OK).json(localAlterado);

        } catch (RegraNegocioException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result(e.getMessage());
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result("ID inválido");
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Erro interno: " + e.getMessage());
        }
    }

    private void deletarLocal(Context ctx) {
        try {
            Long id = Long.parseLong(ctx.pathParam("id"));
            deletarLocalPort.executar(id);
            ctx.status(HttpStatus.NO_CONTENT); // 204
        } catch (RegraNegocioException e) {
            ctx.status(HttpStatus.NOT_FOUND).result(e.getMessage());
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result("ID inválido");
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Erro interno: " + e.getMessage());
        }
    }
}
