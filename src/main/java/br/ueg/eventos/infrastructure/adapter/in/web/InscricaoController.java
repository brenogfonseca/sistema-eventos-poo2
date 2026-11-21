package br.ueg.eventos.infrastructure.adapter.in.web;

import br.ueg.eventos.application.port.in.inscricao.*;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.exception.AcessoNegadoException;
import br.ueg.eventos.domain.model.Inscricao;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

public class InscricaoController {

    private final BuscarInscricaoPorIdPort buscarInscricaoPorIdPort;
    private final ListarInscricoesPorAtividadePort listarPorAtividadePort;
    private final ListarInscricoesPorUsuarioPort listarPorUsuarioPort;
    private final EventoAuthorization autorizacao;

    public InscricaoController(
            BuscarInscricaoPorIdPort buscarInscricaoPorIdPort,
            ListarInscricoesPorAtividadePort listarPorAtividadePort,
            ListarInscricoesPorUsuarioPort listarPorUsuarioPort,
            EventoAuthorization autorizacao) {

        this.buscarInscricaoPorIdPort = buscarInscricaoPorIdPort;
        this.listarPorAtividadePort = listarPorAtividadePort;
        this.listarPorUsuarioPort = listarPorUsuarioPort;
        this.autorizacao = autorizacao;
    }

    public void registerRoutes(Javalin app) {
        app.get("/inscricoes/{id}", this::buscarPorId);
        app.get("/inscricoes/atividade/{idAtividade}",
                this::listarPorAtividade);
        app.get("/inscricoes/usuario/{idUsuario}",
                this::listarPorUsuario);
    }

    private void buscarPorId(Context ctx) {
        try {
            Long id = Long.parseLong(ctx.pathParam("id"));

            Inscricao inscricao =
                    buscarInscricaoPorIdPort.executar(id);
            int executor = autorizacao.usuarioAutenticado(ctx);
            if (!inscricao.getIdUsuario().equals((long) executor) && !autorizacao.ehAdministrador(ctx)) {
                autorizacao.exigirGerenciadorDaAtividade(ctx, inscricao.getIdAtividade());
            }

            ctx.status(HttpStatus.OK).json(inscricao);

        } catch (AcessoNegadoException e) {
            ctx.status(HttpStatus.FORBIDDEN).result(e.getMessage());
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
            autorizacao.exigirGerenciadorDaAtividade(ctx, idAtividade);

            ctx.status(HttpStatus.OK)
                    .json(listarPorAtividadePort.executar(idAtividade));

        } catch (AcessoNegadoException e) {
            ctx.status(HttpStatus.FORBIDDEN).result(e.getMessage());
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
            if (idUsuario != autorizacao.usuarioAutenticado(ctx) && !autorizacao.ehAdministrador(ctx)) {
                throw new AcessoNegadoException("Só é permitido consultar as próprias inscrições.");
            }

            ctx.status(HttpStatus.OK)
                    .json(listarPorUsuarioPort.executar(idUsuario));

        } catch (AcessoNegadoException e) {
            ctx.status(HttpStatus.FORBIDDEN).result(e.getMessage());
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

}
