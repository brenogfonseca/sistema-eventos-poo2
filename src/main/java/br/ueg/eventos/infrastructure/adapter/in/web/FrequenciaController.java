package br.ueg.eventos.infrastructure.adapter.in.web;

import br.ueg.eventos.application.port.in.frequencia.BuscarFrequenciaPorIdPort;
import br.ueg.eventos.application.port.in.frequencia.ListarFrequenciasPorInscricaoPort;
import br.ueg.eventos.application.port.in.frequencia.RegistrarPresencaManualPort;
import br.ueg.eventos.domain.exception.AcessoNegadoException;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Frequencia;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

public class FrequenciaController {
    private final BuscarFrequenciaPorIdPort buscarPort;
    private final ListarFrequenciasPorInscricaoPort listarPort;
    private final RegistrarPresencaManualPort registrarManualPort;

    public FrequenciaController(BuscarFrequenciaPorIdPort buscarPort,
                                ListarFrequenciasPorInscricaoPort listarPort,
                                RegistrarPresencaManualPort registrarManualPort) {
        this.buscarPort = buscarPort;
        this.listarPort = listarPort;
        this.registrarManualPort = registrarManualPort;
    }

    public void registerRoutes(Javalin app) {
        app.post("/frequencias/manual", this::registrarManual);
        app.get("/frequencias/{id}", this::buscarPorId);
        app.get("/inscricoes/{idInscricao}/frequencias", this::listarPorInscricao);
    }

    public static class RegistrarPresencaManualRequest {
        public Long idInscricaoEvento;
        public Long idAtividade;
        public boolean presente;
    }

    private void registrarManual(Context ctx) {
        try {
            RegistrarPresencaManualRequest request = ctx.bodyAsClass(RegistrarPresencaManualRequest.class);
            Integer idResponsavel = extrairResponsavel(ctx);
            Frequencia frequencia = registrarManualPort.executar(
                    request.idInscricaoEvento, request.idAtividade, request.presente, idResponsavel);
            ctx.status(HttpStatus.CREATED).json(frequencia);
        } catch (AcessoNegadoException e) {
            ctx.status(HttpStatus.FORBIDDEN).result(e.getMessage());
        } catch (RegraNegocioException | IllegalArgumentException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result(e.getMessage());
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Erro interno ao registrar a presença manual.");
        }
    }

    private void buscarPorId(Context ctx) {
        try {
            ctx.status(HttpStatus.OK).json(buscarPort.executar(Integer.valueOf(ctx.pathParam("id"))));
        } catch (RegraNegocioException | IllegalArgumentException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result(e.getMessage());
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Erro interno ao buscar frequência.");
        }
    }

    private void listarPorInscricao(Context ctx) {
        try {
            ctx.status(HttpStatus.OK).json(listarPort.executar(Long.valueOf(ctx.pathParam("idInscricao"))));
        } catch (RegraNegocioException | IllegalArgumentException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result(e.getMessage());
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Erro interno ao listar frequências.");
        }
    }

    private Integer extrairResponsavel(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.header("X-Usuario-Id"));
            if (id <= 0) throw new NumberFormatException();
            return id;
        } catch (Exception e) {
            throw new AcessoNegadoException("Informe o organizador autenticado no cabeçalho X-Usuario-Id.");
        }
    }
}
