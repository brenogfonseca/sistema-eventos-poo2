package br.ueg.eventos;

import br.ueg.eventos.application.port.in.atividade.AtualizarAtividadePort;
import br.ueg.eventos.application.port.in.atividade.BuscarAtividadePorIdPort;
import br.ueg.eventos.application.port.in.atividade.CadastrarAtividadePort;
import br.ueg.eventos.application.port.in.atividade.ExcluirAtividadePort;
import br.ueg.eventos.application.port.in.atividade.ListarAtividadesPorEventoPort;
import br.ueg.eventos.application.port.in.evento.AlterarEventoPort;
import br.ueg.eventos.application.port.in.evento.BuscarEventoPorIdPort;
import br.ueg.eventos.application.port.in.evento.BuscarVinculoOrganizadorPort;
import br.ueg.eventos.application.port.in.evento.CriarEventoPort;
import br.ueg.eventos.application.port.in.evento.ExcluirEventoPort;
import br.ueg.eventos.application.port.in.evento.ListarEventosPort;
import br.ueg.eventos.application.port.out.VinculoOrganizadorRepositoryPort;
import br.ueg.eventos.application.service.evento.AlterarEventoService;
import br.ueg.eventos.application.service.evento.BuscarEventoPorIdService;
import br.ueg.eventos.application.service.evento.BuscarVinculoOrganizadorService;
import br.ueg.eventos.application.service.evento.ExcluirEventoService;
import br.ueg.eventos.application.port.in.frequencia.BuscarFrequenciaPorIdPort;
import br.ueg.eventos.application.port.in.frequencia.GerarCheckinQrPort;
import br.ueg.eventos.application.port.in.frequencia.ListarFrequenciasPorInscricaoPort;
import br.ueg.eventos.application.port.in.frequencia.RegistrarCheckinQrPort;
import br.ueg.eventos.application.port.in.frequencia.RegistrarFrequenciaPort;
import br.ueg.eventos.application.port.in.frequencia.RegistrarPresencaManualPort;
import br.ueg.eventos.application.port.in.inscricao.BuscarInscricaoPorIdPort;
import br.ueg.eventos.application.port.in.inscricao.CalcularFrequenciaCertificadoPort;
import br.ueg.eventos.application.port.in.inscricao.EmitirCertificadoPort;
import br.ueg.eventos.application.port.in.inscricao.CancelarInscricaoEventoPort;
import br.ueg.eventos.application.port.in.inscricao.InscreverNoEventoPort;
import br.ueg.eventos.application.port.in.inscricao.SelecionarAtividadesPort;
import br.ueg.eventos.application.port.in.inscricao.ListarInscricoesEventoPorUsuarioPort;
import br.ueg.eventos.application.port.in.inscricao.ListarAtividadesEscolhidasPort;
import br.ueg.eventos.application.port.in.inscricao.ListarInscricoesPorAtividadePort;
import br.ueg.eventos.application.port.in.inscricao.ListarInscricoesPorUsuarioPort;
import br.ueg.eventos.application.port.in.local.AlterarLocalPort;
import br.ueg.eventos.application.port.in.local.BuscarLocalPorIdPort;
import br.ueg.eventos.application.port.in.local.CadastrarLocalPort;
import br.ueg.eventos.application.port.in.local.DeletarLocalPort;
import br.ueg.eventos.application.port.in.local.ListarLocaisPort;
import br.ueg.eventos.application.port.in.pessoa.VincularPessoaProgramacaoPort;
import br.ueg.eventos.application.port.in.pessoa.EmitirCertificadoProgramacaoPort;
import br.ueg.eventos.application.port.in.questao.AtualizarQuestaoPort;
import br.ueg.eventos.application.port.in.questao.BuscarQuestaoNoQuestionarioPort;
import br.ueg.eventos.application.port.in.questao.CriarQuestaoPort;
import br.ueg.eventos.application.port.in.questao.RemoverQuestaoPort;
import br.ueg.eventos.application.port.in.questionario.AtualizarQuestionarioPort;
import br.ueg.eventos.application.port.in.questionario.BuscarQuestionarioPort;
import br.ueg.eventos.application.port.in.questionario.CriarQuestionarioPort;
import br.ueg.eventos.application.port.in.questionario.RemoverQuestionarioPort;
import br.ueg.eventos.application.port.in.questionario.ExportarResultadosQuestionarioPort;
import br.ueg.eventos.application.port.in.resposta.AtualizarRespostaPort;
import br.ueg.eventos.application.port.in.resposta.BuscarRespostaPort;
import br.ueg.eventos.application.port.in.resposta.CriarRespostaPort;
import br.ueg.eventos.application.port.in.resposta.RemoverRespostaPort;
import br.ueg.eventos.application.port.in.relatorio.ExportarRelatorioPort;
import br.ueg.eventos.application.port.in.usuario.AlterarDadosUsuarioPort;
import br.ueg.eventos.application.port.in.usuario.AutenticarUsuarioPort;
import br.ueg.eventos.application.port.in.usuario.AlterarPerfilUsuarioPort;
import br.ueg.eventos.application.port.in.usuario.AlterarSenhaUsuarioPort;
import br.ueg.eventos.application.port.in.usuario.BuscarUsuarioPorEmailPort;
import br.ueg.eventos.application.port.in.usuario.BuscarUsuarioPorIdPort;
import br.ueg.eventos.application.port.in.usuario.CadastrarUsuarioPort;
import br.ueg.eventos.application.port.in.usuario.DeletarUsuarioPort;
import br.ueg.eventos.application.port.in.usuario.ListarUsuariosPort;
import br.ueg.eventos.application.port.out.AtividadeRepositoryPort;
import br.ueg.eventos.application.port.out.AuthenticationTokenPort;
import br.ueg.eventos.application.port.out.CheckinQrTokenSignerPort;
import br.ueg.eventos.application.port.out.CertificadoPdfGeneratorPort;
import br.ueg.eventos.application.port.out.CertificadoRepositoryPort;
import br.ueg.eventos.application.port.out.EventoRepositoryPort;
import br.ueg.eventos.application.port.out.FrequenciaRepositoryPort;
import br.ueg.eventos.application.port.out.InscricaoRepositoryPort;
import br.ueg.eventos.application.port.out.InscricaoEventoRepositoryPort;
import br.ueg.eventos.application.port.out.LocalRepositoryPort;
import br.ueg.eventos.application.port.out.PasswordEncryptor;
import br.ueg.eventos.application.port.out.PessoaProgramacaoRepositoryPort;
import br.ueg.eventos.application.port.out.QuestaoRepositoryPort;
import br.ueg.eventos.application.port.out.QuestionarioRepositoryPort;
import br.ueg.eventos.application.port.out.RelatorioRepositoryPort;
import br.ueg.eventos.application.port.out.RespostaRepositoryPort;
import br.ueg.eventos.application.port.out.UsuarioRepositoryPort;
import br.ueg.eventos.application.service.atividade.AtualizarAtividadeService;
import br.ueg.eventos.application.service.atividade.BuscarAtividadePorIdService;
import br.ueg.eventos.application.service.atividade.CadastrarAtividadeService;
import br.ueg.eventos.application.service.atividade.ExcluirAtividadeService;
import br.ueg.eventos.application.service.atividade.ListarAtividadesPorEventoService;
import br.ueg.eventos.application.service.evento.CriarEventoService;
import br.ueg.eventos.application.service.evento.ListarEventosService;
import br.ueg.eventos.application.service.frequencia.BuscarFrequenciaPorIdService;
import br.ueg.eventos.application.service.frequencia.GerarCheckinQrService;
import br.ueg.eventos.application.service.frequencia.ListarFrequenciasPorInscricaoService;
import br.ueg.eventos.application.service.frequencia.RegistrarCheckinQrService;
import br.ueg.eventos.application.service.frequencia.RegistrarFrequenciaService;
import br.ueg.eventos.application.service.frequencia.RegistrarPresencaManualService;
import br.ueg.eventos.application.service.inscricao.BuscarInscricaoPorIdService;
import br.ueg.eventos.application.service.inscricao.CalcularFrequenciaCertificadoService;
import br.ueg.eventos.application.service.inscricao.EmitirCertificadoService;
import br.ueg.eventos.application.service.inscricao.CancelarInscricaoEventoService;
import br.ueg.eventos.application.service.inscricao.InscreverNoEventoService;
import br.ueg.eventos.application.service.inscricao.SelecionarAtividadesService;
import br.ueg.eventos.application.service.inscricao.ListarInscricoesEventoPorUsuarioService;
import br.ueg.eventos.application.service.inscricao.ListarAtividadesEscolhidasService;
import br.ueg.eventos.application.service.inscricao.ListarInscricoesPorAtividadeService;
import br.ueg.eventos.application.service.inscricao.ListarInscricoesPorUsuarioService;
import br.ueg.eventos.application.service.local.AlterarLocalService;
import br.ueg.eventos.application.service.local.BuscarLocalPorIdService;
import br.ueg.eventos.application.service.local.CadastrarLocalService;
import br.ueg.eventos.application.service.local.DeletarLocalService;
import br.ueg.eventos.application.service.local.ListarLocaisService;
import br.ueg.eventos.application.service.pessoa.VincularPessoaProgramacaoService;
import br.ueg.eventos.application.service.pessoa.EmitirCertificadoProgramacaoService;
import br.ueg.eventos.application.service.questao.AtualizarQuestaoService;
import br.ueg.eventos.application.service.questao.BuscarQuestaoNoQuestionarioService;
import br.ueg.eventos.application.service.questao.CriarQuestaoService;
import br.ueg.eventos.application.service.questao.RemoverQuestaoService;
import br.ueg.eventos.application.service.questionario.AtualizarQuestionarioService;
import br.ueg.eventos.application.service.questionario.BuscarQuestionarioService;
import br.ueg.eventos.application.service.questionario.CriarQuestionarioService;
import br.ueg.eventos.application.service.questionario.RemoverQuestionarioService;
import br.ueg.eventos.application.service.questionario.ExportarResultadosQuestionarioService;
import br.ueg.eventos.application.service.resposta.AtualizarRespostaService;
import br.ueg.eventos.application.service.resposta.BuscarRespostaService;
import br.ueg.eventos.application.service.resposta.CriarRespostaService;
import br.ueg.eventos.application.service.resposta.RemoverRespostaService;
import br.ueg.eventos.application.service.relatorio.ExportarRelatorioService;
import br.ueg.eventos.application.service.usuario.AlterarDadosUsuarioService;
import br.ueg.eventos.application.service.usuario.AutenticarUsuarioService;
import br.ueg.eventos.application.service.usuario.AlterarPerfilUsuarioService;
import br.ueg.eventos.application.service.usuario.AlterarSenhaUsuarioService;
import br.ueg.eventos.application.service.usuario.BuscarUsuarioPorEmailService;
import br.ueg.eventos.application.service.usuario.BuscarUsuarioPorIdService;
import br.ueg.eventos.application.service.usuario.CadastrarUsuarioService;
import br.ueg.eventos.application.service.usuario.DeletarUsuarioService;
import br.ueg.eventos.application.service.usuario.ListarUsuariosService;
import br.ueg.eventos.infrastructure.adapter.in.web.AtividadeController;
import br.ueg.eventos.infrastructure.adapter.in.web.AuthController;
import br.ueg.eventos.infrastructure.adapter.in.web.BearerAuthenticationFilter;
import br.ueg.eventos.infrastructure.adapter.in.web.CheckinQrController;
import br.ueg.eventos.infrastructure.adapter.in.web.CertificadoController;
import br.ueg.eventos.infrastructure.adapter.in.web.EventoController;
import br.ueg.eventos.infrastructure.adapter.in.web.EventoAuthorization;
import br.ueg.eventos.infrastructure.adapter.in.web.FrequenciaController;
import br.ueg.eventos.infrastructure.adapter.in.web.PessoaProgramacaoController;
import br.ueg.eventos.infrastructure.adapter.in.web.InscricaoController;
import br.ueg.eventos.infrastructure.adapter.in.web.InscricaoEventoController;
import br.ueg.eventos.infrastructure.adapter.in.web.LocalController;
import br.ueg.eventos.infrastructure.adapter.in.web.QuestaoController;
import br.ueg.eventos.infrastructure.adapter.in.web.QuestionarioController;
import br.ueg.eventos.infrastructure.adapter.in.web.RespostaController;
import br.ueg.eventos.infrastructure.adapter.in.web.RelatorioController;
import br.ueg.eventos.infrastructure.adapter.in.web.UsuarioController;
import br.ueg.eventos.infrastructure.adapter.out.persistence.Pbkdf2PasswordEncryptor;
import br.ueg.eventos.infrastructure.adapter.out.persistence.PostgresAtividadeRepository;
import br.ueg.eventos.infrastructure.adapter.out.persistence.PostgresConnectionFactory;
import br.ueg.eventos.infrastructure.adapter.out.persistence.PostgresCertificadoRepository;
import br.ueg.eventos.infrastructure.adapter.out.persistence.PostgresDatabaseInitializer;
import br.ueg.eventos.infrastructure.adapter.out.persistence.PostgresEventoRepository;
import br.ueg.eventos.infrastructure.adapter.out.persistence.PostgresFrequenciaRepository;
import br.ueg.eventos.infrastructure.adapter.out.persistence.PostgresInscricaoRepository;
import br.ueg.eventos.infrastructure.adapter.out.persistence.PostgresInscricaoEventoRepository;
import br.ueg.eventos.infrastructure.adapter.out.persistence.PostgresLocalRepository;
import br.ueg.eventos.infrastructure.adapter.out.persistence.PostgresPessoaProgramacaoRepository;
import br.ueg.eventos.infrastructure.adapter.out.persistence.PostgresQuestaoRepository;
import br.ueg.eventos.infrastructure.adapter.out.persistence.PostgresQuestionarioRepository;
import br.ueg.eventos.infrastructure.adapter.out.persistence.PostgresRelatorioRepository;
import br.ueg.eventos.infrastructure.adapter.out.persistence.PostgresRespostaRepository;
import br.ueg.eventos.infrastructure.adapter.out.persistence.PostgresUsuarioRepository;
import br.ueg.eventos.infrastructure.adapter.out.persistence.PostgresVinculoOrganizadorRepository;
import br.ueg.eventos.infrastructure.adapter.out.qrcode.ZxingQrCodeGenerator;
import br.ueg.eventos.infrastructure.adapter.out.pdf.SimpleCertificatePdfGenerator;
import br.ueg.eventos.infrastructure.adapter.out.security.HmacCheckinQrTokenSigner;
import br.ueg.eventos.infrastructure.adapter.out.security.HmacAuthenticationTokenSigner;
import br.ueg.eventos.infrastructure.config.ApplicationEnvironment;
import br.ueg.eventos.domain.model.Perfil;
import br.ueg.eventos.domain.model.Usuario;
import io.javalin.Javalin;

public class Main {
    public static void main(String[] args) {
        System.out.println(
                "Iniciando Plataforma de Gestão de Eventos "
                        + "(Hexagonal sem Framework pesado no Core)..."
        );

        PostgresConnectionFactory connectionFactory =
                new PostgresConnectionFactory();
        new PostgresDatabaseInitializer(connectionFactory).inicializar();

        System.out.println("Banco PostgreSQL inicializado.");

        // Repositórios
        EventoRepositoryPort eventoRepository =
                new PostgresEventoRepository(connectionFactory);
        AtividadeRepositoryPort atividadeRepository =
                new PostgresAtividadeRepository(connectionFactory);
        InscricaoRepositoryPort inscricaoRepository =
                new PostgresInscricaoRepository(connectionFactory);
        InscricaoEventoRepositoryPort inscricaoEventoRepository =
                new PostgresInscricaoEventoRepository(connectionFactory);
        FrequenciaRepositoryPort frequenciaRepository =
                new PostgresFrequenciaRepository(connectionFactory);
        UsuarioRepositoryPort usuarioRepository =
                new PostgresUsuarioRepository(connectionFactory);
        PasswordEncryptor passwordEncryptor =
                new Pbkdf2PasswordEncryptor();
        provisionarAdministradorInicial(usuarioRepository, passwordEncryptor);
        AuthenticationTokenPort authenticationTokenPort =
                new HmacAuthenticationTokenSigner();
        AutenticarUsuarioPort autenticarUsuarioPort = new AutenticarUsuarioService(
                usuarioRepository, passwordEncryptor, authenticationTokenPort);
        AuthController authController = new AuthController(autenticarUsuarioPort);
        LocalRepositoryPort localRepository =
                new PostgresLocalRepository(connectionFactory);
        QuestionarioRepositoryPort questionarioRepository =
                new PostgresQuestionarioRepository(connectionFactory);
        QuestaoRepositoryPort questaoRepository =
                new PostgresQuestaoRepository(connectionFactory);
        RespostaRepositoryPort respostaRepository =
                new PostgresRespostaRepository(connectionFactory);
        RelatorioRepositoryPort relatorioRepository =
                new PostgresRelatorioRepository(connectionFactory);
        CertificadoRepositoryPort certificadoRepository =
                new PostgresCertificadoRepository(connectionFactory);
        CertificadoPdfGeneratorPort certificadoPdfGenerator =
                new SimpleCertificatePdfGenerator();
        VinculoOrganizadorRepositoryPort vinculoOrganizadorRepository =
                new PostgresVinculoOrganizadorRepository(connectionFactory);
        PessoaProgramacaoRepositoryPort pessoaProgramacaoRepository =
                new PostgresPessoaProgramacaoRepository(connectionFactory);
        EventoAuthorization eventoAuthorization = new EventoAuthorization(
                atividadeRepository,
                eventoRepository,
                usuarioRepository,
                vinculoOrganizadorRepository,
                questionarioRepository,
                questaoRepository
        );

        // Evento
        CriarEventoPort criarEventoPort =
                new CriarEventoService(eventoRepository, vinculoOrganizadorRepository);
        ListarEventosPort listarEventosPort =
                new ListarEventosService(eventoRepository);
        AlterarEventoPort alterarEventoPort =
                new AlterarEventoService(eventoRepository, vinculoOrganizadorRepository,
                        inscricaoEventoRepository, atividadeRepository);
        ExcluirEventoPort excluirEventoPort =
                new ExcluirEventoService(eventoRepository, vinculoOrganizadorRepository);
        BuscarEventoPorIdPort buscarEventoPorIdPort =
                new BuscarEventoPorIdService(eventoRepository);
        BuscarVinculoOrganizadorPort buscarVinculoOrganizadorPort =
                new BuscarVinculoOrganizadorService(vinculoOrganizadorRepository);

        EventoController eventoController =
                new EventoController(
                        criarEventoPort,
                        listarEventosPort,
                        alterarEventoPort,
                        excluirEventoPort,
                        buscarEventoPorIdPort,
                        buscarVinculoOrganizadorPort,
                        usuarioRepository
                );

        // Atividade
        CadastrarAtividadePort cadastrarAtividadePort =
                new CadastrarAtividadeService(
                        atividadeRepository,
                        eventoRepository,
                        usuarioRepository,
                        vinculoOrganizadorRepository
                );
        AtualizarAtividadePort atualizarAtividadePort =
                new AtualizarAtividadeService(
                        atividadeRepository,
                        eventoRepository,
                        usuarioRepository,
                        vinculoOrganizadorRepository
                );
        ExcluirAtividadePort excluirAtividadePort =
                new ExcluirAtividadeService(atividadeRepository, inscricaoRepository);
        BuscarAtividadePorIdPort buscarAtividadePorIdPort =
                new BuscarAtividadePorIdService(atividadeRepository, eventoRepository);
        ListarAtividadesPorEventoPort listarAtividadesPorEventoPort =
                new ListarAtividadesPorEventoService(atividadeRepository);
        AtividadeController atividadeController = new AtividadeController(
                cadastrarAtividadePort,
                atualizarAtividadePort,
                excluirAtividadePort,
                buscarAtividadePorIdPort,
                listarAtividadesPorEventoPort,
                eventoAuthorization
        );

        // Inscrição
        InscreverNoEventoPort inscreverNoEventoPort =
                new InscreverNoEventoService(
                        eventoRepository,
                        inscricaoEventoRepository,
                        usuarioRepository
                );
        SelecionarAtividadesPort selecionarAtividadesPort =
                new SelecionarAtividadesService(
                        inscricaoEventoRepository,
                        inscricaoRepository,
                        atividadeRepository,
                        eventoRepository
                );
        ListarInscricoesEventoPorUsuarioPort listarInscricoesEventoPorUsuarioPort =
                new ListarInscricoesEventoPorUsuarioService(inscricaoEventoRepository);
        ListarAtividadesEscolhidasPort listarAtividadesEscolhidasPort =
                new ListarAtividadesEscolhidasService(inscricaoEventoRepository, inscricaoRepository);
        BuscarInscricaoPorIdPort buscarInscricaoPorIdPort =
                new BuscarInscricaoPorIdService(inscricaoRepository);
        ListarInscricoesPorAtividadePort listarInscricoesPorAtividadePort =
                new ListarInscricoesPorAtividadeService(
                        inscricaoRepository,
                        atividadeRepository
                );
        ListarInscricoesPorUsuarioPort listarInscricoesPorUsuarioPort =
                new ListarInscricoesPorUsuarioService(inscricaoRepository);
        InscricaoController inscricaoController = new InscricaoController(
                buscarInscricaoPorIdPort,
                listarInscricoesPorAtividadePort,
                listarInscricoesPorUsuarioPort,
                eventoAuthorization
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
        RegistrarPresencaManualPort registrarPresencaManualPort =
                new RegistrarPresencaManualService(
                        inscricaoEventoRepository,
                        inscricaoRepository,
                        atividadeRepository,
                        usuarioRepository,
                        vinculoOrganizadorRepository,
                        registrarFrequenciaPort
                );
        FrequenciaController frequenciaController = new FrequenciaController(
                buscarFrequenciaPorIdPort,
                listarFrequenciasPorInscricaoPort,
                registrarPresencaManualPort,
                inscricaoRepository,
                eventoAuthorization
        );
        CalcularFrequenciaCertificadoPort calcularFrequenciaCertificadoPort =
                new CalcularFrequenciaCertificadoService(
                        inscricaoEventoRepository,
                        inscricaoRepository,
                        eventoRepository,
                        atividadeRepository,
                        frequenciaRepository
                );
        EmitirCertificadoPort emitirCertificadoPort = new EmitirCertificadoService(
                inscricaoEventoRepository,
                eventoRepository,
                usuarioRepository,
                calcularFrequenciaCertificadoPort,
                certificadoRepository,
                certificadoPdfGenerator
        );
        EmitirCertificadoProgramacaoPort emitirCertificadoProgramacaoPort =
                new EmitirCertificadoProgramacaoService(
                        eventoRepository,
                        atividadeRepository,
                        pessoaProgramacaoRepository,
                        certificadoRepository,
                        certificadoPdfGenerator
                );
        CertificadoController certificadoController = new CertificadoController(
                emitirCertificadoPort,
                emitirCertificadoProgramacaoPort,
                eventoAuthorization
        );
        CancelarInscricaoEventoPort cancelarInscricaoEventoPort =
                new CancelarInscricaoEventoService(
                        inscricaoEventoRepository,
                        inscricaoRepository,
                        atividadeRepository,
                        eventoRepository
                );
        InscricaoEventoController inscricaoEventoController =
                new InscricaoEventoController(
                        inscreverNoEventoPort,
                        listarInscricoesEventoPorUsuarioPort,
                        selecionarAtividadesPort,
                        listarAtividadesEscolhidasPort,
                        calcularFrequenciaCertificadoPort,
                        cancelarInscricaoEventoPort
                );
        CheckinQrTokenSignerPort checkinQrTokenSigner =
                new HmacCheckinQrTokenSigner();
        GerarCheckinQrPort gerarCheckinQrPort =
                new GerarCheckinQrService(
                        inscricaoEventoRepository,
                        eventoRepository,
                        checkinQrTokenSigner
                );
        RegistrarCheckinQrPort registrarCheckinQrPort =
                new RegistrarCheckinQrService(
                        checkinQrTokenSigner,
                        inscricaoEventoRepository,
                        inscricaoRepository,
                        atividadeRepository,
                        frequenciaRepository,
                        usuarioRepository,
                        vinculoOrganizadorRepository,
                        registrarFrequenciaPort
                );
        CheckinQrController checkinQrController = new CheckinQrController(
                gerarCheckinQrPort,
                registrarCheckinQrPort,
                new ZxingQrCodeGenerator()
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

        // Questionário
        CriarQuestionarioPort criarQuestionarioPort =
                new CriarQuestionarioService(questionarioRepository);
        BuscarQuestionarioPort buscarQuestionarioPort =
                new BuscarQuestionarioService(questionarioRepository);
        AtualizarQuestionarioPort atualizarQuestionarioPort =
                new AtualizarQuestionarioService(questionarioRepository, questaoRepository);
        RemoverQuestionarioPort removerQuestionarioPort =
                new RemoverQuestionarioService(questionarioRepository);
        ExportarResultadosQuestionarioPort exportarResultadosQuestionarioPort =
                new ExportarResultadosQuestionarioService(
                        questionarioRepository, questaoRepository, respostaRepository);
        QuestionarioController questionarioController =
                new QuestionarioController(
                        criarQuestionarioPort,
                        buscarQuestionarioPort,
                        atualizarQuestionarioPort,
                        removerQuestionarioPort,
                        eventoAuthorization,
                        exportarResultadosQuestionarioPort
                );

        // Questão
        CriarQuestaoPort criarQuestaoPort =
                new CriarQuestaoService(questaoRepository);
        AtualizarQuestaoPort atualizarQuestaoPort =
                new AtualizarQuestaoService(questaoRepository);
        RemoverQuestaoPort removerQuestaoPort =
                new RemoverQuestaoService(questaoRepository);
        BuscarQuestaoNoQuestionarioPort buscarQuestaoNoQuestionarioPort =
                new BuscarQuestaoNoQuestionarioService(questaoRepository);
        QuestaoController questaoController = new QuestaoController(
                criarQuestaoPort,
                atualizarQuestaoPort,
                removerQuestaoPort,
                buscarQuestaoNoQuestionarioPort,
                eventoAuthorization
        );

        // Resposta
        CriarRespostaPort criarRespostaPort =
                new CriarRespostaService(
                        respostaRepository,
                        questaoRepository,
                        questionarioRepository,
                        inscricaoRepository,
                        frequenciaRepository,
                        atividadeRepository
                );
        AtualizarRespostaPort atualizarRespostaPort =
                new AtualizarRespostaService(respostaRepository);
        RemoverRespostaPort removerRespostaPort =
                new RemoverRespostaService(respostaRepository);
        BuscarRespostaPort buscarRespostaPort =
                new BuscarRespostaService(respostaRepository);
        RespostaController respostaController = new RespostaController(
                criarRespostaPort,
                atualizarRespostaPort,
                removerRespostaPort,
                buscarRespostaPort,
                questaoRepository,
                questionarioRepository,
                eventoAuthorization
        );

        ExportarRelatorioPort exportarRelatorioPort = new ExportarRelatorioService(
                eventoRepository,
                usuarioRepository,
                vinculoOrganizadorRepository,
                relatorioRepository
        );
        RelatorioController relatorioController = new RelatorioController(
                exportarRelatorioPort,
                eventoAuthorization
        );

        // Pessoas vinculadas à programação (palestrantes, apresentadores e responsáveis)
        VincularPessoaProgramacaoPort vincularPessoaProgramacaoPort =
                new VincularPessoaProgramacaoService(
                        atividadeRepository,
                        eventoRepository,
                        usuarioRepository,
                        vinculoOrganizadorRepository,
                        pessoaProgramacaoRepository
                );
        PessoaProgramacaoController pessoaProgramacaoController =
                new PessoaProgramacaoController(vincularPessoaProgramacaoPort, eventoAuthorization);

        // Servidor e rotas
        Javalin app = Javalin.create(config -> {
            config.showJavalinBanner = false;
        });

        new BearerAuthenticationFilter(authenticationTokenPort, usuarioRepository).install(app);

        authController.registerRoutes(app);
        eventoController.registerRoutes(app);
        atividadeController.registerRoutes(app);
        inscricaoController.registerRoutes(app);
        inscricaoEventoController.registerRoutes(app);
        frequenciaController.registerRoutes(app);
        checkinQrController.registerRoutes(app);
        certificadoController.registerRoutes(app);
        usuarioController.registerRoutes(app);
        localController.registerRoutes(app);
        questionarioController.registerRoutes(app);
        questaoController.registerRoutes(app);
        respostaController.registerRoutes(app);
        relatorioController.registerRoutes(app);
        pessoaProgramacaoController.registerRoutes(app);

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

    private static void provisionarAdministradorInicial(
            UsuarioRepositoryPort usuarios,
            PasswordEncryptor senhas) {
        String email = ApplicationEnvironment.get("EVENTOS_ADMIN_EMAIL");
        String senha = ApplicationEnvironment.get("EVENTOS_ADMIN_PASSWORD");
        if ((email == null || email.isBlank()) && (senha == null || senha.isBlank())) return;
        if (email == null || email.isBlank() || senha == null || senha.isBlank()) {
            throw new IllegalStateException(
                    "Configure EVENTOS_ADMIN_EMAIL e EVENTOS_ADMIN_PASSWORD juntos para criar o primeiro administrador.");
        }
        boolean jaExiste = usuarios.listarTodos().stream()
                .anyMatch(usuario -> usuario.getPerfil() == Perfil.ADMINISTRADOR);
        if (jaExiste) return;
        String nome = ApplicationEnvironment.get("EVENTOS_ADMIN_NAME");
        if (nome == null || nome.isBlank()) nome = "Administrador";
        Usuario administrador = new Usuario(
                nome, email.trim(), senha, senhas.criptografar(senha), Perfil.ADMINISTRADOR);
        usuarios.salvar(administrador);
        System.out.println("Administrador inicial criado a partir da configuração de ambiente.");
    }
}
