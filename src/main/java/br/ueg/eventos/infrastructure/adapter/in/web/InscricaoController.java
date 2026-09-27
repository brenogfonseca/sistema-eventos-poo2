package br.ueg.eventos.infrastructure.adapter.in.web;

import br.ueg.eventos.application.port.in.inscricao.*;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Inscricao;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

public class InscricaoController {

    private final InscreverUsuarioPort inscreverUsuarioPort;
    private final BuscarInscricaoPorIdPort buscarInscricaoPorIdPort;
    private final ListarInscricoesPorAtividadePort listarPorAtividadePort;
    private final ListarInscricoesPorUsuarioPort listarPorUsuarioPort;
    private final CancelarInscricaoPort cancelarInscricaoPort;
    private final ReativarInscricaoPort reativarInscricaoPort;

    public InscricaoController(
            InscreverUsuarioPort inscreverUsuarioPort,
            BuscarInscricaoPorIdPort buscarInscricaoPorIdPort,
            ListarInscricoesPorAtividadePort listarPorAtividadePort,
            ListarInscricoesPorUsuarioPort listarPorUsuarioPort,
            CancelarInscricaoPort cancelarInscricaoPort,
            ReativarInscricaoPort reativarInscricaoPort) {

        this.inscreverUsuarioPort = inscreverUsuarioPort;
        this.buscarInscricaoPorIdPort = buscarInscricaoPorIdPort;
        this.listarPorAtividadePort = listarPorAtividadePort;
        this.listarPorUsuarioPort = listarPorUsuarioPort;
        this.cancelarInscricaoPort = cancelarInscricaoPort;
        this.reativarInscricaoPort = reativarInscricaoPort;
    }

    public void registerRoutes(Javalin app) {
        app.post("/inscricoes", this::inscrever);
        app.get("/inscricoes/{id}", this::buscarPorId);
        app.get("/inscricoes/atividade/{idAtividade}",
                this::listarPorAtividade);
        app.get("/inscricoes/usuario/{idUsuario}",
                this::listarPorUsuario);
        app.patch("/inscricoes/{id}/cancelar",
                this::cancelar);
        app.patch("/inscricoes/{id}/reativar",
                this::reativar);
    }

    static class InscricaoRequest {
        public Long idAtividade;
        public Long idUsuario;
    }

    private void inscrever(Context ctx) {
        try {
            InscricaoRequest request =
                    ctx.bodyAsClass(InscricaoRequest.class);

            Inscricao inscricao = inscreverUsuarioPort.executar(
                    request.idAtividade,
                    request.idUsuario
            );

            ctx.status(HttpStatus.CREATED).json(inscricao);

        } catch (RegraNegocioException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result(e.getMessage());
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .result("Erro interno: " + e.getMessage());
        }
    }

    private void buscarPorId(Context ctx) {
        try {
            Long id = Long.parseLong(ctx.pathParam("id"));

            Inscricao inscricao =
                    buscarInscricaoPorIdPort.executar(id);

            ctx.status(HttpStatus.OK).json(inscricao);

        } catch (RegraNegocioException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result(e.getMessage());
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .result("O id deve ser um número válido.");
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .result("Erro interno: " + e.getMessage());
        }
    }

    private void listarPorAtividade(Context ctx) {
        try {
            Long idAtividade =
                    Long.parseLong(ctx.pathParam("idAtividade"));

            ctx.status(HttpStatus.OK)
                    .json(listarPorAtividadePort.executar(idAtividade));

        } catch (RegraNegocioException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result(e.getMessage());
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .result("O id da atividade deve ser válido.");
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .result("Erro interno: " + e.getMessage());
        }
    }

    private void listarPorUsuario(Context ctx) {
        try {
            Long idUsuario =
                    Long.parseLong(ctx.pathParam("idUsuario"));

            ctx.status(HttpStatus.OK)
                    .json(listarPorUsuarioPort.executar(idUsuario));

        } catch (RegraNegocioException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result(e.getMessage());
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .result("O id do usuário deve ser válido.");
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .result("Erro interno: " + e.getMessage());
        }
    }

    private void cancelar(Context ctx) {
        try {
            Long id = Long.parseLong(ctx.pathParam("id"));

            cancelarInscricaoPort.executar(id);

            ctx.status(HttpStatus.NO_CONTENT);

        } catch (RegraNegocioException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result(e.getMessage());
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .result("O id deve ser um número válido.");
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .result("Erro interno: " + e.getMessage());
        }
    }

    private void reativar(Context ctx) {
        try {
            Long id = Long.parseLong(ctx.pathParam("id"));

            Inscricao inscricao =
                    reativarInscricaoPort.executar(id);

            ctx.status(HttpStatus.OK).json(inscricao);

        } catch (RegraNegocioException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result(e.getMessage());
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .result("O id deve ser um número válido.");
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .result("Erro interno: " + e.getMessage());
        }
    }
}