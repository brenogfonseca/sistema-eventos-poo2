package br.ueg.eventos.infrastructure.adapter.in.web;

import br.ueg.eventos.application.port.in.evento.CriarEventoPort;
import br.ueg.eventos.application.port.in.evento.CriarEventoPort.ComandoCriarEvento;
import br.ueg.eventos.application.port.in.evento.ListarEventosPort;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Evento;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.time.LocalDateTime;

public class EventoController {

    private final CriarEventoPort criarEventoPort;
    private final ListarEventosPort listarEventosPort;

    public EventoController(CriarEventoPort criarEventoPort, ListarEventosPort listarEventosPort) {
        this.criarEventoPort = criarEventoPort;
        this.listarEventosPort = listarEventosPort;
    }

    public void registerRoutes(Javalin app) {
        app.post("/eventos", this::criarEvento);
        app.get("/eventos", this::listarEventos);
    }

    static class CriarEventoRequest {
        public String titulo;
        public String descricao;
        public String inicio;
        public String fim;
        public int capacidade;
    }

    private void listarEventos(Context ctx) {
        try {
            ctx.status(HttpStatus.OK).json(listarEventosPort.executar());
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Erro interno: " + e.getMessage());
        }
    }

    private void criarEvento(Context ctx) {
        try {
            CriarEventoRequest request = ctx.bodyAsClass(CriarEventoRequest.class);

            ComandoCriarEvento comando = new ComandoCriarEvento(
                request.titulo,
                request.descricao,
                LocalDateTime.parse(request.inicio),
                LocalDateTime.parse(request.fim),
                request.capacidade
            );

            Evento eventoCriado = criarEventoPort.executar(comando);

            ctx.status(HttpStatus.CREATED).json(eventoCriado);

        } catch (RegraNegocioException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result(e.getMessage());
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Erro interno: " + e.getMessage());
        }
    }
}
