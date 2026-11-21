package br.ueg.eventos.infrastructure.adapter.in.web;

import br.ueg.eventos.application.port.in.relatorio.ExportarRelatorioPort;
import br.ueg.eventos.domain.exception.AcessoNegadoException;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

public final class RelatorioController {
    private final ExportarRelatorioPort exportar;
    private final EventoAuthorization autorizacao;

    public RelatorioController(ExportarRelatorioPort exportar, EventoAuthorization autorizacao) {
        this.exportar = exportar;
        this.autorizacao = autorizacao;
    }

    public void registerRoutes(Javalin app) {
        app.get("/relatorios/eventos/{id}/inscricoes.csv", this::inscricoes);
        app.get("/relatorios/eventos/{id}/frequencias.csv", this::frequencias);
    }

    private void inscricoes(Context ctx) {
        try {
            long idEvento = Long.parseLong(ctx.pathParam("id"));
            String csv = exportar.inscricoesCsv(idEvento, autorizacao.usuarioAutenticado(ctx));
            ctx.status(HttpStatus.OK).contentType("text/csv; charset=utf-8")
                    .header("Content-Disposition", "attachment; filename=inscricoes-evento-" + idEvento + ".csv")
                    .result(csv);
        } catch (AcessoNegadoException e) {
            ctx.status(HttpStatus.FORBIDDEN).result(e.getMessage());
        } catch (RegraNegocioException | NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result(e.getMessage());
        }
    }

    private void frequencias(Context ctx) {
        try {
            long idEvento = Long.parseLong(ctx.pathParam("id"));
            String csv = exportar.frequenciasCsv(idEvento, autorizacao.usuarioAutenticado(ctx));
            ctx.status(HttpStatus.OK).contentType("text/csv; charset=utf-8")
                    .header("Content-Disposition", "attachment; filename=frequencias-evento-" + idEvento + ".csv")
                    .result(csv);
        } catch (AcessoNegadoException e) {
            ctx.status(HttpStatus.FORBIDDEN).result(e.getMessage());
        } catch (RegraNegocioException | NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result(e.getMessage());
        }
    }
}
