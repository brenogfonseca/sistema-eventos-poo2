package br.ueg.eventos.infrastructure.adapter.in.web;

import br.ueg.eventos.application.port.in.frequencia.AtualizarPresencaPort;
import br.ueg.eventos.application.port.in.frequencia.BuscarFrequenciaPorIdPort;
import br.ueg.eventos.application.port.in.frequencia.ListarFrequenciasPorInscricaoPort;
import br.ueg.eventos.application.port.in.frequencia.RegistrarFrequenciaPort;
import br.ueg.eventos.application.port.in.frequencia.RegistrarFrequenciaPort.ComandoRegistrarFrequencia;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Frequencia;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.time.LocalDateTime;

public class FrequenciaController {

    private final RegistrarFrequenciaPort registrarFrequenciaPort;
    private final BuscarFrequenciaPorIdPort buscarFrequenciaPorIdPort;
    private final ListarFrequenciasPorInscricaoPort listarFrequenciasPorInscricaoPort;
    private final AtualizarPresencaPort atualizarPresencaPort;

    public FrequenciaController(
            RegistrarFrequenciaPort registrarFrequenciaPort,
            BuscarFrequenciaPorIdPort buscarFrequenciaPorIdPort,
            ListarFrequenciasPorInscricaoPort listarFrequenciasPorInscricaoPort,
            AtualizarPresencaPort atualizarPresencaPort
    ) {
        this.registrarFrequenciaPort = registrarFrequenciaPort;
        this.buscarFrequenciaPorIdPort = buscarFrequenciaPorIdPort;
        this.listarFrequenciasPorInscricaoPort =
                listarFrequenciasPorInscricaoPort;
        this.atualizarPresencaPort = atualizarPresencaPort;
    }

    public void registerRoutes(Javalin app) {
        app.post("/frequencias", this::registrar);
        app.get("/frequencias/{id}", this::buscarPorId);
        app.get(
                "/inscricoes/{idInscricao}/frequencias",
                this::listarPorInscricao
        );
        app.patch(
                "/frequencias/{id}/presenca",
                this::atualizarPresenca
        );
    }

    public static class RegistrarFrequenciaRequest {
        public String dataHora;
        public String origem;
        public Integer idResponsavel;
        public boolean presente;
        public Long idInscricao;
        public String tipo;
    }

    public static class AtualizarPresencaRequest {
        public boolean presente;
    }

    private void registrar(Context ctx) {
        try {
            RegistrarFrequenciaRequest request =
                    ctx.bodyAsClass(RegistrarFrequenciaRequest.class);

            ComandoRegistrarFrequencia comando =
                    new ComandoRegistrarFrequencia(
                            LocalDateTime.parse(request.dataHora),
                            request.origem,
                            request.idResponsavel,
                            request.presente,
                            request.idInscricao,
                            request.tipo
                    );

            Frequencia frequencia = registrarFrequenciaPort.executar(comando);

            ctx.status(HttpStatus.CREATED).json(frequencia);
        } catch (RegraNegocioException | IllegalArgumentException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result(e.getMessage());
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .result("Erro interno: " + e.getMessage());
        }
    }

    private void buscarPorId(Context ctx) {
        try {
            Integer id = Integer.valueOf(ctx.pathParam("id"));
            ctx.status(HttpStatus.OK)
                    .json(buscarFrequenciaPorIdPort.executar(id));
        } catch (RegraNegocioException | IllegalArgumentException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result(e.getMessage());
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .result("Erro interno: " + e.getMessage());
        }
    }

    private void listarPorInscricao(Context ctx) {
        try {
            Long idInscricao =
                    Long.valueOf(ctx.pathParam("idInscricao"));

            ctx.status(HttpStatus.OK).json(
                    listarFrequenciasPorInscricaoPort.executar(idInscricao)
            );
        } catch (RegraNegocioException | IllegalArgumentException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result(e.getMessage());
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .result("Erro interno: " + e.getMessage());
        }
    }

    private void atualizarPresenca(Context ctx) {
        try {
            Integer id = Integer.valueOf(ctx.pathParam("id"));
            AtualizarPresencaRequest request =
                    ctx.bodyAsClass(AtualizarPresencaRequest.class);

            ctx.status(HttpStatus.OK).json(
                    atualizarPresencaPort.executar(id, request.presente)
            );
        } catch (RegraNegocioException | IllegalArgumentException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result(e.getMessage());
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .result("Erro interno: " + e.getMessage());
        }
    }
}