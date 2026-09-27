package br.ueg.eventos;

import br.ueg.eventos.application.port.in.evento.CriarEventoPort;
import br.ueg.eventos.application.port.in.evento.ListarEventosPort;
import br.ueg.eventos.application.port.in.usuario.AlterarDadosUsuarioPort;
import br.ueg.eventos.application.port.in.usuario.AlterarPerfilUsuarioPort;
import br.ueg.eventos.application.port.in.usuario.AlterarSenhaUsuarioPort;
import br.ueg.eventos.application.port.in.usuario.BuscarUsuarioPorEmailPort;
import br.ueg.eventos.application.port.in.usuario.BuscarUsuarioPorIdPort;
import br.ueg.eventos.application.port.in.usuario.CadastrarUsuarioPort;
import br.ueg.eventos.application.port.in.usuario.DeletarUsuarioPort;
import br.ueg.eventos.application.port.in.usuario.ListarUsuariosPort;
import br.ueg.eventos.application.port.out.EventoRepositoryPort;
import br.ueg.eventos.application.port.out.PasswordEncryptor;
import br.ueg.eventos.application.port.out.UsuarioRepositoryPort;
import br.ueg.eventos.application.service.evento.CriarEventoService;
import br.ueg.eventos.application.service.evento.ListarEventosService;
import br.ueg.eventos.application.service.usuario.AlterarDadosUsuarioService;
import br.ueg.eventos.application.service.usuario.AlterarPerfilUsuarioService;
import br.ueg.eventos.application.service.usuario.AlterarSenhaUsuarioService;
import br.ueg.eventos.application.service.usuario.BuscarUsuarioPorEmailService;
import br.ueg.eventos.application.service.usuario.BuscarUsuarioPorIdService;
import br.ueg.eventos.application.service.usuario.CadastrarUsuarioService;
import br.ueg.eventos.application.service.usuario.DeletarUsuarioService;
import br.ueg.eventos.application.service.usuario.ListarUsuariosService;
import br.ueg.eventos.infrastructure.adapter.in.web.EventoController;
import br.ueg.eventos.infrastructure.adapter.in.web.UsuarioController;
import br.ueg.eventos.infrastructure.adapter.out.persistence.InMemoryEventoRepository;
import br.ueg.eventos.infrastructure.adapter.out.persistence.InMemoryUsuarioRepository;
import br.ueg.eventos.infrastructure.adapter.out.persistence.Sha256PasswordEncryptor;
import io.javalin.Javalin;

public class Main {
    public static void main(String[] args) {
        System.out.println("Iniciando Plataforma de Gestão de Eventos (Hexagonal sem Framework pesado no Core)...");

        EventoRepositoryPort
         eventoRepository = new InMemoryEventoRepository();

        CriarEventoPort criarEventoPort = new CriarEventoService(eventoRepository);
        ListarEventosPort listarEventosPort = new ListarEventosService(eventoRepository);

        EventoController eventoController = new EventoController(criarEventoPort, listarEventosPort);

        // --- Composição: Usuario ---
        UsuarioRepositoryPort usuarioRepository = new InMemoryUsuarioRepository();
        PasswordEncryptor passwordEncryptor = new Sha256PasswordEncryptor();

        CadastrarUsuarioPort cadastrarUsuarioPort = new CadastrarUsuarioService(usuarioRepository, passwordEncryptor);
        BuscarUsuarioPorEmailPort buscarPorEmailPort = new BuscarUsuarioPorEmailService(usuarioRepository);
        BuscarUsuarioPorIdPort buscarPorIdPort = new BuscarUsuarioPorIdService(usuarioRepository);
        ListarUsuariosPort listarUsuariosPort = new ListarUsuariosService(usuarioRepository);
        AlterarDadosUsuarioPort alterarDadosPort = new AlterarDadosUsuarioService(usuarioRepository);
        AlterarSenhaUsuarioPort alterarSenhaPort = new AlterarSenhaUsuarioService(usuarioRepository, passwordEncryptor);
        AlterarPerfilUsuarioPort alterarPerfilPort = new AlterarPerfilUsuarioService(usuarioRepository);
        DeletarUsuarioPort deletarPort = new DeletarUsuarioService(usuarioRepository);

        UsuarioController usuarioController = new UsuarioController(
                cadastrarUsuarioPort,
                buscarPorEmailPort,
                buscarPorIdPort,
                listarUsuariosPort,
                alterarDadosPort,
                alterarSenhaPort,
                alterarPerfilPort,
                deletarPort);

        // --- Servidor ---
        Javalin app = Javalin.create(config -> {
            config.showJavalinBanner = false;
        });

        eventoController.registerRoutes(app);
        usuarioController.registerRoutes(app);

        app.start(8080);

        System.out.println("Servidor iniciado na porta 8080.");
        System.out.println("Para testar, envie um POST para http://localhost:8080/eventos");
        System.out.println("Body ex: { \"titulo\": \"Symposium POO\", \"descricao\": \"...\", \"inicio\": \"2026-10-01T08:00:00\", \"fim\": \"2026-10-03T18:00:00\", \"capacidade\": 100 }");
    }
}
