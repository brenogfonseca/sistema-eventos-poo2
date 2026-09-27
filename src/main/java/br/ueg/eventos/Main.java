package br.ueg.eventos;

import br.ueg.eventos.application.port.in.evento.CriarEventoPort;
import br.ueg.eventos.application.port.in.evento.ListarEventosPort;
import br.ueg.eventos.application.port.in.usuario.AlterarSenhaUsuarioPort;
import br.ueg.eventos.application.port.in.usuario.BuscarUsuarioPorEmailPort;
import br.ueg.eventos.application.port.in.usuario.CadastrarUsuarioPort;
import br.ueg.eventos.application.port.out.EventoRepository;
import br.ueg.eventos.application.port.out.PasswordEncryptor;
import br.ueg.eventos.application.port.out.UsuarioRepository;
import br.ueg.eventos.application.service.evento.CriarEventoService;
import br.ueg.eventos.application.service.evento.ListarEventosService;
import br.ueg.eventos.application.service.usuario.AlterarSenhaUsuarioService;
import br.ueg.eventos.application.service.usuario.BuscarUsuarioPorEmailService;
import br.ueg.eventos.application.service.usuario.CadastrarUsuarioService;
import br.ueg.eventos.infrastructure.adapter.in.web.EventoController;
import br.ueg.eventos.infrastructure.adapter.in.web.UsuarioController;
import br.ueg.eventos.infrastructure.adapter.out.persistence.InMemoryEventoRepository;
import br.ueg.eventos.infrastructure.adapter.out.persistence.InMemoryUsuarioRepository;
import br.ueg.eventos.infrastructure.adapter.out.persistence.Sha256PasswordEncryptor;
import io.javalin.Javalin;

public class Main {
    public static void main(String[] args) {
        System.out.println("Iniciando Plataforma de Gestão de Eventos (Hexagonal sem Framework pesado no Core)...");

        // --- Composição: Evento ---
        EventoRepository eventoRepository = new InMemoryEventoRepository();

        CriarEventoPort criarEventoPort = new CriarEventoService(eventoRepository);
        ListarEventosPort listarEventosPort = new ListarEventosService(eventoRepository);

        EventoController eventoController = new EventoController(criarEventoPort, listarEventosPort);

        // --- Composição: Usuario ---
        UsuarioRepository usuarioRepository = new InMemoryUsuarioRepository();
        PasswordEncryptor passwordEncryptor = new Sha256PasswordEncryptor();

        CadastrarUsuarioPort cadastrarUsuarioPort = new CadastrarUsuarioService(usuarioRepository, passwordEncryptor);
        BuscarUsuarioPorEmailPort buscarUsuarioPorEmailPort = new BuscarUsuarioPorEmailService(usuarioRepository);
        AlterarSenhaUsuarioPort alterarSenhaUsuarioPort = new AlterarSenhaUsuarioService(usuarioRepository, passwordEncryptor);

        UsuarioController usuarioController = new UsuarioController(
                cadastrarUsuarioPort,
                buscarUsuarioPorEmailPort,
                alterarSenhaUsuarioPort
        );

        // --- Servidor ---
        Javalin app = Javalin.create(config -> {
            config.showJavalinBanner = false;
        });

        eventoController.registerRoutes(app);
        usuarioController.registerRoutes(app);

        app.start(8080);

        System.out.println("Servidor iniciado na porta 8080.");
        System.out.println("Rotas de Evento  → POST /eventos | GET /eventos");
        System.out.println("Rotas de Usuario → POST /usuarios | GET /usuarios/email/{email} | PATCH /usuarios/{id}/senha");
    }
}
