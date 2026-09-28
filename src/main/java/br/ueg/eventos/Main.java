package br.ueg.eventos;

import br.ueg.eventos.application.port.in.atividade.AtualizarAtividadePort;
import br.ueg.eventos.application.port.in.atividade.BuscarAtividadePorIdPort;
import br.ueg.eventos.application.port.in.atividade.CadastrarAtividadePort;
import br.ueg.eventos.application.port.in.atividade.ExcluirAtividadePort;
import br.ueg.eventos.application.port.in.atividade.ListarAtividadesPorEventoPort;
import br.ueg.eventos.application.port.in.evento.CriarEventoPort;
import br.ueg.eventos.application.port.in.evento.ListarEventosPort;
import br.ueg.eventos.application.port.in.frequencia.AtualizarPresencaPort;
import br.ueg.eventos.application.port.in.frequencia.BuscarFrequenciaPorIdPort;
import br.ueg.eventos.application.port.in.frequencia.ListarFrequenciasPorInscricaoPort;
import br.ueg.eventos.application.port.in.frequencia.RegistrarFrequenciaPort;
import br.ueg.eventos.application.port.in.inscricao.BuscarInscricaoPorIdPort;
import br.ueg.eventos.application.port.in.inscricao.CancelarInscricaoPort;
import br.ueg.eventos.application.port.in.inscricao.InscreverUsuarioPort;
import br.ueg.eventos.application.port.in.inscricao.ListarInscricoesPorAtividadePort;
import br.ueg.eventos.application.port.in.inscricao.ListarInscricoesPorUsuarioPort;
import br.ueg.eventos.application.port.in.inscricao.ReativarInscricaoPort;
import br.ueg.eventos.application.port.in.usuario.AlterarDadosUsuarioPort;
import br.ueg.eventos.application.port.in.usuario.AlterarPerfilUsuarioPort;
import br.ueg.eventos.application.port.in.usuario.AlterarSenhaUsuarioPort;
import br.ueg.eventos.application.port.in.usuario.BuscarUsuarioPorEmailPort;
import br.ueg.eventos.application.port.in.usuario.BuscarUsuarioPorIdPort;
import br.ueg.eventos.application.port.in.usuario.CadastrarUsuarioPort;
import br.ueg.eventos.application.port.in.usuario.DeletarUsuarioPort;
import br.ueg.eventos.application.port.in.usuario.ListarUsuariosPort;
import br.ueg.eventos.application.port.out.AtividadeRepositoryPort;
import br.ueg.eventos.application.port.out.EventoRepositoryPort;
import br.ueg.eventos.application.port.out.FrequenciaRepositoryPort;
import br.ueg.eventos.application.port.out.InscricaoRepositoryPort;
import br.ueg.eventos.application.port.out.PasswordEncryptor;
import br.ueg.eventos.application.port.out.UsuarioRepositoryPort;
import br.ueg.eventos.application.service.atividade.AtualizarAtividadeService;
import br.ueg.eventos.application.service.atividade.BuscarAtividadePorIdService;
import br.ueg.eventos.application.service.atividade.CadastrarAtividadeService;
import br.ueg.eventos.application.service.atividade.ExcluirAtividadeService;
import br.ueg.eventos.application.service.atividade.ListarAtividadesPorEventoService;
import br.ueg.eventos.application.service.evento.CriarEventoService;
import br.ueg.eventos.application.service.evento.ListarEventosService;
import br.ueg.eventos.application.service.frequencia.AtualizarPresencaService;
import br.ueg.eventos.application.service.frequencia.BuscarFrequenciaPorIdService;
import br.ueg.eventos.application.service.frequencia.ListarFrequenciasPorInscricaoService;
import br.ueg.eventos.application.service.frequencia.RegistrarFrequenciaService;
import br.ueg.eventos.application.service.inscricao.BuscarInscricaoPorIdService;
import br.ueg.eventos.application.service.inscricao.CancelarInscricaoService;
import br.ueg.eventos.application.service.inscricao.InscreverUsuarioService;
import br.ueg.eventos.application.service.inscricao.ListarInscricoesPorAtividadeService;
import br.ueg.eventos.application.service.inscricao.ListarInscricoesPorUsuarioService;
import br.ueg.eventos.application.service.inscricao.ReativarInscricaoService;
import br.ueg.eventos.application.service.usuario.AlterarDadosUsuarioService;
import br.ueg.eventos.application.service.usuario.AlterarPerfilUsuarioService;
import br.ueg.eventos.application.service.usuario.AlterarSenhaUsuarioService;
import br.ueg.eventos.application.service.usuario.BuscarUsuarioPorEmailService;
import br.ueg.eventos.application.service.usuario.BuscarUsuarioPorIdService;
import br.ueg.eventos.application.service.usuario.CadastrarUsuarioService;
import br.ueg.eventos.application.service.usuario.DeletarUsuarioService;
import br.ueg.eventos.application.service.usuario.ListarUsuariosService;
import br.ueg.eventos.infrastructure.adapter.in.web.AtividadeController;
import br.ueg.eventos.infrastructure.adapter.in.web.EventoController;
import br.ueg.eventos.infrastructure.adapter.in.web.FrequenciaController;
import br.ueg.eventos.infrastructure.adapter.in.web.InscricaoController;
import br.ueg.eventos.infrastructure.adapter.in.web.UsuarioController;
import br.ueg.eventos.infrastructure.adapter.out.persistence.InMemoryAtividadeRepository;
import br.ueg.eventos.infrastructure.adapter.out.persistence.InMemoryEventoRepository;
import br.ueg.eventos.infrastructure.adapter.out.persistence.InMemoryFrequenciaRepository;
import br.ueg.eventos.infrastructure.adapter.out.persistence.InMemoryInscricaoRepository;
import br.ueg.eventos.infrastructure.adapter.out.persistence.InMemoryUsuarioRepository;
import br.ueg.eventos.infrastructure.adapter.out.persistence.Sha256PasswordEncryptor;
import br.ueg.eventos.application.port.in.local.AlterarLocalPort;
import br.ueg.eventos.application.port.in.local.BuscarLocalPorIdPort;
import br.ueg.eventos.application.port.in.local.CadastrarLocalPort;
import br.ueg.eventos.application.port.in.local.DeletarLocalPort;
import br.ueg.eventos.application.port.in.local.ListarLocaisPort;
import br.ueg.eventos.application.port.out.LocalRepositoryPort;
import br.ueg.eventos.application.service.local.AlterarLocalService;
import br.ueg.eventos.application.service.local.BuscarLocalPorIdService;
import br.ueg.eventos.application.service.local.CadastrarLocalService;
import br.ueg.eventos.application.service.local.DeletarLocalService;
import br.ueg.eventos.application.service.local.ListarLocaisService;
import br.ueg.eventos.infrastructure.adapter.in.web.LocalController;
import br.ueg.eventos.infrastructure.adapter.out.persistence.InMemoryLocalRepository;
import io.javalin.Javalin;

public class Main {
    public static void main(String[] args) {
        System.out.println(
                "Iniciando Plataforma de Gestão de Eventos "
                        + "(Hexagonal sem Framework pesado no Core)..."
        );

        // Repositórios
        EventoRepositoryPort eventoRepository =
                new InMemoryEventoRepository();
        AtividadeRepositoryPort atividadeRepository =
                new InMemoryAtividadeRepository();
        InscricaoRepositoryPort inscricaoRepository =
                new InMemoryInscricaoRepository();
        FrequenciaRepositoryPort frequenciaRepository =
                new InMemoryFrequenciaRepository();
        UsuarioRepositoryPort usuarioRepository =
                new InMemoryUsuarioRepository();
        PasswordEncryptor passwordEncryptor =
                new Sha256PasswordEncryptor();
        LocalRepositoryPort localRepository =
                new InMemoryLocalRepository();

        // Evento
        CriarEventoPort criarEventoPort =
                new CriarEventoService(eventoRepository);
        ListarEventosPort listarEventosPort =
                new ListarEventosService(eventoRepository);
        EventoController eventoController =
                new EventoController(criarEventoPort, listarEventosPort);

        // Atividade
        CadastrarAtividadePort cadastrarAtividadePort =
                new CadastrarAtividadeService(
                        atividadeRepository,
                        eventoRepository
                );
        AtualizarAtividadePort atualizarAtividadePort =
                new AtualizarAtividadeService(
                        atividadeRepository,
                        eventoRepository
                );
        ExcluirAtividadePort excluirAtividadePort =
                new ExcluirAtividadeService(atividadeRepository);
        BuscarAtividadePorIdPort buscarAtividadePorIdPort =
                new BuscarAtividadePorIdService(atividadeRepository);
        ListarAtividadesPorEventoPort listarAtividadesPorEventoPort =
                new ListarAtividadesPorEventoService(
                        atividadeRepository,
                        eventoRepository
                );
        AtividadeController atividadeController = new AtividadeController(
                cadastrarAtividadePort,
                atualizarAtividadePort,
                excluirAtividadePort,
                buscarAtividadePorIdPort,
                listarAtividadesPorEventoPort
        );

        // Inscrição
        InscreverUsuarioPort inscreverUsuarioPort =
                new InscreverUsuarioService(
                        inscricaoRepository,
                        atividadeRepository,
                        eventoRepository
                );
        BuscarInscricaoPorIdPort buscarInscricaoPorIdPort =
                new BuscarInscricaoPorIdService(inscricaoRepository);
        ListarInscricoesPorAtividadePort listarInscricoesPorAtividadePort =
                new ListarInscricoesPorAtividadeService(
                        inscricaoRepository,
                        atividadeRepository
                );
        ListarInscricoesPorUsuarioPort listarInscricoesPorUsuarioPort =
                new ListarInscricoesPorUsuarioService(inscricaoRepository);
        CancelarInscricaoPort cancelarInscricaoPort =
                new CancelarInscricaoService(
                        inscricaoRepository,
                        atividadeRepository
                );
        ReativarInscricaoPort reativarInscricaoPort =
                new ReativarInscricaoService(
                        inscricaoRepository,
                        atividadeRepository,
                        eventoRepository
                );
        InscricaoController inscricaoController = new InscricaoController(
                inscreverUsuarioPort,
                buscarInscricaoPorIdPort,
                listarInscricoesPorAtividadePort,
                listarInscricoesPorUsuarioPort,
                cancelarInscricaoPort,
                reativarInscricaoPort
        );

        // Frequência
        RegistrarFrequenciaPort registrarFrequenciaPort =
                new RegistrarFrequenciaService(
                        frequenciaRepository,
                        inscricaoRepository
                );
        BuscarFrequenciaPorIdPort buscarFrequenciaPorIdPort =
                new BuscarFrequenciaPorIdService(frequenciaRepository);
        ListarFrequenciasPorInscricaoPort listarFrequenciasPorInscricaoPort =
                new ListarFrequenciasPorInscricaoService(
                        frequenciaRepository,
                        inscricaoRepository
                );
        AtualizarPresencaPort atualizarPresencaPort =
                new AtualizarPresencaService(frequenciaRepository);
        FrequenciaController frequenciaController = new FrequenciaController(
                registrarFrequenciaPort,
                buscarFrequenciaPorIdPort,
                listarFrequenciasPorInscricaoPort,
                atualizarPresencaPort
        );

        // Usuário
        CadastrarUsuarioPort cadastrarUsuarioPort =
                new CadastrarUsuarioService(
                        usuarioRepository,
                        passwordEncryptor
                );
        BuscarUsuarioPorEmailPort buscarPorEmailPort =
                new BuscarUsuarioPorEmailService(usuarioRepository);
        BuscarUsuarioPorIdPort buscarPorIdPort =
                new BuscarUsuarioPorIdService(usuarioRepository);
        ListarUsuariosPort listarUsuariosPort =
                new ListarUsuariosService(usuarioRepository);
        AlterarDadosUsuarioPort alterarDadosPort =
                new AlterarDadosUsuarioService(usuarioRepository);
        AlterarSenhaUsuarioPort alterarSenhaPort =
                new AlterarSenhaUsuarioService(
                        usuarioRepository,
                        passwordEncryptor
                );
        AlterarPerfilUsuarioPort alterarPerfilPort =
                new AlterarPerfilUsuarioService(usuarioRepository);
        DeletarUsuarioPort deletarPort =
                new DeletarUsuarioService(usuarioRepository);
        UsuarioController usuarioController = new UsuarioController(
                cadastrarUsuarioPort,
                buscarPorEmailPort,
                buscarPorIdPort,
                listarUsuariosPort,
                alterarDadosPort,
                alterarSenhaPort,
                alterarPerfilPort,
                deletarPort
        );

        // Local
        CadastrarLocalPort cadastrarLocalPort =
                new CadastrarLocalService(localRepository);
        ListarLocaisPort listarLocaisPort =
                new ListarLocaisService(localRepository);
        BuscarLocalPorIdPort buscarLocalPorIdPort =
                new BuscarLocalPorIdService(localRepository);
        AlterarLocalPort alterarLocalPort =
                new AlterarLocalService(localRepository);
        DeletarLocalPort deletarLocalPort =
                new DeletarLocalService(localRepository);
        LocalController localController = new LocalController(
                cadastrarLocalPort,
                listarLocaisPort,
                buscarLocalPorIdPort,
                alterarLocalPort,
                deletarLocalPort
        );

        // Servidor e rotas
        Javalin app = Javalin.create(config -> {
            config.showJavalinBanner = false;
        });

        eventoController.registerRoutes(app);
        atividadeController.registerRoutes(app);
        inscricaoController.registerRoutes(app);
        frequenciaController.registerRoutes(app);
        usuarioController.registerRoutes(app);
        localController.registerRoutes(app);

        app.start(8080);

        System.out.println("Servidor iniciado na porta 8080.");
        System.out.println(
                "Para testar, envie um POST para http://localhost:8080/eventos"
        );
        System.out.println(
                "Body ex: { \"titulo\": \"Symposium POO\", "
                        + "\"descricao\": \"...\", "
                        + "\"inicio\": \"2026-10-01T08:00:00\", "
                        + "\"fim\": \"2026-10-03T18:00:00\", "
                        + "\"capacidade\": 100 }"
        );
    }
}
