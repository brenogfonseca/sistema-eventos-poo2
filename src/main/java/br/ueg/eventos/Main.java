package br.ueg.eventos;

import br.ueg.eventos.application.port.in.evento.CriarEventoPort;
import br.ueg.eventos.application.port.in.evento.ListarEventosPort;
import br.ueg.eventos.application.port.out.EventoRepository;
import br.ueg.eventos.application.service.evento.CriarEventoService;
import br.ueg.eventos.application.service.evento.ListarEventosService;
import br.ueg.eventos.infrastructure.adapter.in.web.EventoController;
import br.ueg.eventos.infrastructure.adapter.out.persistence.InMemoryEventoRepository;
import io.javalin.Javalin;

public class Main {
    public static void main(String[] args) {
        System.out.println("Iniciando Plataforma de Gestão de Eventos (Hexagonal sem Framework pesado no Core)...");

        EventoRepository eventoRepository = new InMemoryEventoRepository();

        CriarEventoPort criarEventoPort = new CriarEventoService(eventoRepository);
        ListarEventosPort listarEventosPort = new ListarEventosService(eventoRepository);
        
        EventoController eventoController = new EventoController(criarEventoPort, listarEventosPort);

        Javalin app = Javalin.create(config -> {
            config.showJavalinBanner = false;
        });

        eventoController.registerRoutes(app);

        app.start(8080);
        
        System.out.println("Servidor iniciado na porta 8080.");
        System.out.println("Para testar, envie um POST para http://localhost:8080/eventos");
        System.out.println("Body ex: { \"titulo\": \"Symposium POO\", \"descricao\": \"...\", \"inicio\": \"2026-10-01T08:00:00\", \"fim\": \"2026-10-03T18:00:00\", \"capacidade\": 100 }");
    }
}
