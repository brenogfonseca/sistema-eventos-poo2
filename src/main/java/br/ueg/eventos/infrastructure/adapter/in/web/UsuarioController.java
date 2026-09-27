package br.ueg.eventos.infrastructure.adapter.in.web;

import br.ueg.eventos.application.port.in.usuario.AlterarDadosUsuarioPort;
import br.ueg.eventos.application.port.in.usuario.AlterarDadosUsuarioPort.ComandoAlterarDados;
import br.ueg.eventos.application.port.in.usuario.AlterarPerfilUsuarioPort;
import br.ueg.eventos.application.port.in.usuario.AlterarPerfilUsuarioPort.ComandoAlterarPerfil;
import br.ueg.eventos.application.port.in.usuario.AlterarSenhaUsuarioPort;
import br.ueg.eventos.application.port.in.usuario.AlterarSenhaUsuarioPort.ComandoAlterarSenha;
import br.ueg.eventos.application.port.in.usuario.BuscarUsuarioPorEmailPort;
import br.ueg.eventos.application.port.in.usuario.BuscarUsuarioPorIdPort;
import br.ueg.eventos.application.port.in.usuario.BuscarUsuarioPorIdPort.ComandoBuscarUsuarioPorId;
import br.ueg.eventos.application.port.in.usuario.CadastrarUsuarioPort;
import br.ueg.eventos.application.port.in.usuario.CadastrarUsuarioPort.ComandoCadastrarUsuario;
import br.ueg.eventos.application.port.in.usuario.DeletarUsuarioPort;
import br.ueg.eventos.application.port.in.usuario.DeletarUsuarioPort.ComandoDeletarUsuario;
import br.ueg.eventos.application.port.in.usuario.ListarUsuariosPort;
import br.ueg.eventos.application.port.in.usuario.ListarUsuariosPort.ComandoListarUsuarios;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Perfil;
import br.ueg.eventos.domain.model.Usuario;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Adaptador de entrada (lado web): traduz requisições HTTP em comandos
 * para os ports de entrada do caso de uso de Usuario.
 *
 * <p>Controle de acesso por perfil é implementado via header {@code X-Usuario-Id},
 * que identifica o usuário logado que está realizando a requisição.</p>
 *
 * <p>Este controller NÃO contém lógica de negócio — toda validação de permissão
 * ocorre nos services ou no próprio domínio.</p>
 */
public class UsuarioController {

    private static final String HEADER_EXECUTOR_ID = "X-Usuario-Id";

    private final CadastrarUsuarioPort cadastrarUsuarioPort;
    private final BuscarUsuarioPorEmailPort buscarUsuarioPorEmailPort;
    private final BuscarUsuarioPorIdPort buscarUsuarioPorIdPort;
    private final ListarUsuariosPort listarUsuariosPort;
    private final AlterarDadosUsuarioPort alterarDadosUsuarioPort;
    private final AlterarSenhaUsuarioPort alterarSenhaUsuarioPort;
    private final AlterarPerfilUsuarioPort alterarPerfilUsuarioPort;
    private final DeletarUsuarioPort deletarUsuarioPort;

    public UsuarioController(
            CadastrarUsuarioPort cadastrarUsuarioPort,
            BuscarUsuarioPorEmailPort buscarUsuarioPorEmailPort,
            BuscarUsuarioPorIdPort buscarUsuarioPorIdPort,
            ListarUsuariosPort listarUsuariosPort,
            AlterarDadosUsuarioPort alterarDadosUsuarioPort,
            AlterarSenhaUsuarioPort alterarSenhaUsuarioPort,
            AlterarPerfilUsuarioPort alterarPerfilUsuarioPort,
            DeletarUsuarioPort deletarUsuarioPort) {
        this.cadastrarUsuarioPort = cadastrarUsuarioPort;
        this.buscarUsuarioPorEmailPort = buscarUsuarioPorEmailPort;
        this.buscarUsuarioPorIdPort = buscarUsuarioPorIdPort;
        this.listarUsuariosPort = listarUsuariosPort;
        this.alterarDadosUsuarioPort = alterarDadosUsuarioPort;
        this.alterarSenhaUsuarioPort = alterarSenhaUsuarioPort;
        this.alterarPerfilUsuarioPort = alterarPerfilUsuarioPort;
        this.deletarUsuarioPort = deletarUsuarioPort;
    }

    public void registerRoutes(Javalin app) {
        // Rota pública: qualquer um pode se cadastrar
        app.post("/usuarios", this::cadastrarUsuario);

        // Rotas autenticadas (exigem X-Usuario-Id no header)
        app.get("/usuarios", this::listarUsuarios);
        app.get("/usuarios/{id}", this::buscarPorId);
        app.get("/usuarios/email/{email}", this::buscarPorEmail);
        app.put("/usuarios/{id}/dados", this::alterarDados);
        app.patch("/usuarios/{id}/senha", this::alterarSenha);
        app.patch("/usuarios/{id}/perfil", this::alterarPerfil);
        app.delete("/usuarios/{id}", this::deletarUsuario);
    }

    // =========================================================================
    // Request bodies (DTOs de entrada — apenas na infraestrutura)
    // =========================================================================

    static class CadastrarUsuarioRequest {
        public String nome;
        public String email;
        public String senha;
        public String perfil;
    }

    static class AlterarDadosRequest {
        public String novoNome;
        public String novoEmail;
    }

    static class AlterarSenhaRequest {
        public String novaSenha;
    }

    static class AlterarPerfilRequest {
        public String novoPerfil;
    }

    // =========================================================================
    // Handlers
    // =========================================================================

    /** POST /usuarios — público, qualquer visitante pode se cadastrar. */
    private void cadastrarUsuario(Context ctx) {
        try {
            exigirCorpoNaoVazio(ctx, "{ \"nome\", \"email\", \"senha\", \"perfil\" }");

            CadastrarUsuarioRequest req = ctx.bodyAsClass(CadastrarUsuarioRequest.class);
            Perfil perfil = parsePerfil(req.perfil);

            Usuario criado = cadastrarUsuarioPort.executar(
                    new ComandoCadastrarUsuario(req.nome, req.email, req.senha, perfil));

            ctx.status(HttpStatus.CREATED).json(new UsuarioResponse(criado));

        } catch (RegraNegocioException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result(e.getMessage());
        } catch (IllegalArgumentException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result(
                    "Perfil inválido. Valores aceitos: VISITANTE, USUARIO, ADMINISTRADOR.");
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Erro interno: " + e.getMessage());
        }
    }

    /** GET /usuarios — apenas ADMINISTRADOR. */
    private void listarUsuarios(Context ctx) {
        try {
            Integer idExecutor = extrairIdExecutor(ctx);

            List<UsuarioResponse> resposta = listarUsuariosPort
                    .executar(new ComandoListarUsuarios(idExecutor))
                    .stream()
                    .map(UsuarioResponse::new)
                    .collect(Collectors.toList());

            ctx.status(HttpStatus.OK).json(resposta);

        } catch (RegraNegocioException e) {
            ctx.status(HttpStatus.FORBIDDEN).result(e.getMessage());
        } catch (IllegalArgumentException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result(
                    "Header X-Usuario-Id deve ser um número inteiro válido.");
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Erro interno: " + e.getMessage());
        }
    }

    /** GET /usuarios/{id} — ADMINISTRADOR ou o próprio usuário. */
    private void buscarPorId(Context ctx) {
        try {
            Integer idExecutor = extrairIdExecutor(ctx);
            Integer idAlvo = Integer.parseInt(ctx.pathParam("id"));

            Usuario usuario = buscarUsuarioPorIdPort.executar(
                    new ComandoBuscarUsuarioPorId(idExecutor, idAlvo));

            ctx.status(HttpStatus.OK).json(new UsuarioResponse(usuario));

        } catch (RegraNegocioException e) {
            ctx.status(HttpStatus.FORBIDDEN).result(e.getMessage());
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result("O id deve ser um número inteiro válido.");
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Erro interno: " + e.getMessage());
        }
    }

    /** GET /usuarios/email/{email} — ADMINISTRADOR. */
    private void buscarPorEmail(Context ctx) {
        try {
            String email = ctx.pathParam("email");

            Usuario usuario = buscarUsuarioPorEmailPort.executar(email)
                    .orElseThrow(() -> new RegraNegocioException(
                            "Usuário não encontrado com o e-mail: " + email));

            ctx.status(HttpStatus.OK).json(new UsuarioResponse(usuario));

        } catch (RegraNegocioException e) {
            ctx.status(HttpStatus.NOT_FOUND).result(e.getMessage());
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Erro interno: " + e.getMessage());
        }
    }

    /** PUT /usuarios/{id}/dados — ADMINISTRADOR ou o próprio usuário. */
    private void alterarDados(Context ctx) {
        try {
            Integer idExecutor = extrairIdExecutor(ctx);
            Integer idAlvo = Integer.parseInt(ctx.pathParam("id"));
            exigirCorpoNaoVazio(ctx, "{ \"novoNome\", \"novoEmail\" }");

            AlterarDadosRequest req = ctx.bodyAsClass(AlterarDadosRequest.class);

            Usuario atualizado = alterarDadosUsuarioPort.executar(
                    new ComandoAlterarDados(idExecutor, idAlvo, req.novoNome, req.novoEmail));

            ctx.status(HttpStatus.OK).json(new UsuarioResponse(atualizado));

        } catch (RegraNegocioException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result(e.getMessage());
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result("O id deve ser um número inteiro válido.");
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Erro interno: " + e.getMessage());
        }
    }

    /** PATCH /usuarios/{id}/senha — ADMINISTRADOR ou o próprio usuário. */
    private void alterarSenha(Context ctx) {
        try {
            Integer idAlvo = Integer.parseInt(ctx.pathParam("id"));
            exigirCorpoNaoVazio(ctx, "{ \"novaSenha\": \"...\" }");

            AlterarSenhaRequest req = ctx.bodyAsClass(AlterarSenhaRequest.class);

            Usuario atualizado = alterarSenhaUsuarioPort.executar(
                    new ComandoAlterarSenha(idAlvo, req.novaSenha));

            ctx.status(HttpStatus.OK).json(new UsuarioResponse(atualizado));

        } catch (RegraNegocioException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result(e.getMessage());
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result("O id deve ser um número inteiro válido.");
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Erro interno: " + e.getMessage());
        }
    }

    /** PATCH /usuarios/{id}/perfil — apenas ADMINISTRADOR. */
    private void alterarPerfil(Context ctx) {
        try {
            Integer idExecutor = extrairIdExecutor(ctx);
            Integer idAlvo = Integer.parseInt(ctx.pathParam("id"));
            exigirCorpoNaoVazio(ctx, "{ \"novoPerfil\": \"USUARIO | ADMINISTRADOR | VISITANTE\" }");

            AlterarPerfilRequest req = ctx.bodyAsClass(AlterarPerfilRequest.class);
            Perfil novoPerfil = parsePerfil(req.novoPerfil);

            Usuario atualizado = alterarPerfilUsuarioPort.executar(
                    new ComandoAlterarPerfil(idExecutor, idAlvo, novoPerfil));

            ctx.status(HttpStatus.OK).json(new UsuarioResponse(atualizado));

        } catch (RegraNegocioException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result(e.getMessage());
        } catch (IllegalArgumentException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result(
                    "Perfil inválido. Valores aceitos: VISITANTE, USUARIO, ADMINISTRADOR.");
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Erro interno: " + e.getMessage());
        }
    }

    /** DELETE /usuarios/{id} — apenas ADMINISTRADOR. */
    private void deletarUsuario(Context ctx) {
        try {
            Integer idExecutor = extrairIdExecutor(ctx);
            Integer idAlvo = Integer.parseInt(ctx.pathParam("id"));

            deletarUsuarioPort.executar(new ComandoDeletarUsuario(idExecutor, idAlvo));

            ctx.status(HttpStatus.NO_CONTENT);

        } catch (RegraNegocioException e) {
            ctx.status(HttpStatus.FORBIDDEN).result(e.getMessage());
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result("O id deve ser um número inteiro válido.");
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Erro interno: " + e.getMessage());
        }
    }

    // =========================================================================
    // Helpers privados
    // =========================================================================

    /**
     * Extrai o id do executor do header {@code X-Usuario-Id}.
     * Lança {@link IllegalArgumentException} se o header estiver ausente ou inválido.
     */
    private Integer extrairIdExecutor(Context ctx) {
        String headerValue = ctx.header(HEADER_EXECUTOR_ID);
        if (headerValue == null || headerValue.isBlank()) {
            throw new RegraNegocioException(
                    "Header '" + HEADER_EXECUTOR_ID + "' é obrigatório para esta operação.");
        }
        return Integer.parseInt(headerValue.trim());
    }

    /** Interrompe o handler com 400 se o corpo da requisição estiver vazio. */
    private void exigirCorpoNaoVazio(Context ctx, String exemplo) {
        if (ctx.body().isEmpty()) {
            ctx.status(HttpStatus.BAD_REQUEST).result(
                    "O corpo da requisição está vazio. Informe: " + exemplo);
            throw new IllegalStateException("Corpo vazio — resposta já enviada.");
        }
    }

    /** Converte string para {@link Perfil}, lançando {@link IllegalArgumentException} se inválido. */
    private Perfil parsePerfil(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("O campo 'perfil' é obrigatório.");
        }
        return Perfil.valueOf(valor.trim().toUpperCase());
    }

    // =========================================================================
    // Response body (DTO de saída — omite o hash da senha por segurança)
    // =========================================================================

    static class UsuarioResponse {
        public final Integer id;
        public final String nome;
        public final String email;
        public final String perfil;

        UsuarioResponse(Usuario usuario) {
            this.id = usuario.getId();
            this.nome = usuario.getNome();
            this.email = usuario.getEmail();
            this.perfil = usuario.getPerfil().name();
            // senhaHash é deliberadamente omitido da resposta
        }
    }
}
