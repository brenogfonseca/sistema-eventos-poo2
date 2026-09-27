package br.ueg.eventos.infrastructure.adapter.in.web;

import br.ueg.eventos.application.port.in.atividade.*;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Atividade;
import br.ueg.eventos.domain.model.Local;
import br.ueg.eventos.domain.model.TipoFrequenciaEnum;
import br.ueg.eventos.domain.model.Trilha;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public class AtividadeController {

    private final CadastrarAtividadePort cadastrarAtividadePort;
    private final AtualizarAtividadePort atualizarAtividadePort;
    private final ExcluirAtividadePort excluirAtividadePort;
    private final BuscarAtividadePorIdPort buscarAtividadePorIdPort;
    private final ListarAtividadesPorEventoPort listarAtividadesPorEventoPort;

    public AtividadeController(
            CadastrarAtividadePort cadastrarAtividadePort,
            AtualizarAtividadePort atualizarAtividadePort,
            ExcluirAtividadePort excluirAtividadePort,
            BuscarAtividadePorIdPort buscarAtividadePorIdPort,
            ListarAtividadesPorEventoPort listarAtividadesPorEventoPort) {

        this.cadastrarAtividadePort = cadastrarAtividadePort;
        this.atualizarAtividadePort = atualizarAtividadePort;
        this.excluirAtividadePort = excluirAtividadePort;
        this.buscarAtividadePorIdPort = buscarAtividadePorIdPort;
        this.listarAtividadesPorEventoPort = listarAtividadesPorEventoPort;
    }

    public void registerRoutes(Javalin app) {
        app.post("/atividades", this::cadastrar);
        app.put("/atividades/{id}", this::atualizar);
        app.delete("/atividades/{id}", this::excluir);
        app.get("/atividades/{id}", this::buscarPorId);
        app.get("/atividades/evento/{idEvento}",
                this::listarPorEvento);
    }

    static class AtividadeRequest {
        public String nome;
        public Long eventoId;
        public Local local;
        public String horaInicio;
        public String horaFim;
        public String data;
        public boolean controlaVagas;
        public int vagas;
        public String tipo;
        public TipoFrequenciaEnum tipoFrequencia;
        public Trilha trilha;
    }

    private Atividade converter(AtividadeRequest request, Long id) {
        return new Atividade(
                id,
                request.nome,
                request.eventoId,
                request.local,
                LocalTime.parse(request.horaInicio),
                LocalTime.parse(request.horaFim),
                LocalDate.parse(request.data),
                request.controlaVagas,
                request.vagas,
                request.tipo,
                request.tipoFrequencia,
                request.trilha
        );
    }

    private void cadastrar(Context ctx) {
        try {
            AtividadeRequest request =
                    ctx.bodyAsClass(AtividadeRequest.class);

            Atividade atividade = converter(request, null);

            Atividade cadastrada =
                    cadastrarAtividadePort.executar(atividade);

            ctx.status(HttpStatus.CREATED).json(cadastrada);

        } catch (RegraNegocioException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result(e.getMessage());
        } catch (java.time.format.DateTimeParseException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .result("Data ou horário inválido.");
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .result("Erro interno: " + e.getMessage());
        }
    }

    private void atualizar(Context ctx) {
        try {
            Long id = Long.parseLong(ctx.pathParam("id"));

            AtividadeRequest request =
                    ctx.bodyAsClass(AtividadeRequest.class);

            Atividade atividade = converter(request, id);

            Atividade atualizada =
                    atualizarAtividadePort.executar(id, atividade);

            ctx.status(HttpStatus.OK).json(atualizada);

        } catch (RegraNegocioException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result(e.getMessage());
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .result("O id da atividade deve ser válido.");
        } catch (java.time.format.DateTimeParseException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .result("Data ou horário inválido.");
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .result("Erro interno: " + e.getMessage());
        }
    }

    private void excluir(Context ctx) {
        try {
            Long id = Long.parseLong(ctx.pathParam("id"));

            excluirAtividadePort.executar(id);

            ctx.status(HttpStatus.NO_CONTENT);

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

    private void buscarPorId(Context ctx) {
        try {
            Long id = Long.parseLong(ctx.pathParam("id"));

            Atividade atividade =
                    buscarAtividadePorIdPort.executar(id);

            ctx.status(HttpStatus.OK).json(atividade);

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

    private void listarPorEvento(Context ctx) {
        try {
            Long idEvento =
                    Long.parseLong(ctx.pathParam("idEvento"));

            ctx.status(HttpStatus.OK)
                    .json(listarAtividadesPorEventoPort.executar(idEvento));

        } catch (RegraNegocioException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result(e.getMessage());
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .result("O id do evento deve ser válido.");
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .result("Erro interno: " + e.getMessage());
        }
    }
}