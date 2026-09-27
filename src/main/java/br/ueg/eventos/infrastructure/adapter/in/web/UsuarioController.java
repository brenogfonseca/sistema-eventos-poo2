package br.ueg.eventos.infrastructure.adapter.in.web;

import br.ueg.eventos.application.port.in.usuario.AlterarSenhaUsuarioPort;
import br.ueg.eventos.application.port.in.usuario.AlterarSenhaUsuarioPort.ComandoAlterarSenha;
import br.ueg.eventos.application.port.in.usuario.BuscarUsuarioPorEmailPort;
import br.ueg.eventos.application.port.in.usuario.CadastrarUsuarioPort;
import br.ueg.eventos.application.port.in.usuario.CadastrarUsuarioPort.ComandoCadastrarUsuario;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Perfil;
import br.ueg.eventos.domain.model.Usuario;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

/**
 * Adaptador de entrada (lado web): traduz requisições HTTP em comandos
 * para os ports de entrada do caso de uso de Usuario.
 * Fica na infraestrutura e NÃO contém lógica de negócio.
 */
public class UsuarioController {

    private final CadastrarUsuarioPort cadastrarUsuarioPort;
    private final BuscarUsuarioPorEmailPort buscarUsuarioPorEmailPort;
    private final AlterarSenhaUsuarioPort alterarSenhaUsuarioPort;

    public UsuarioController(
            CadastrarUsuarioPort cadastrarUsuarioPort,
            BuscarUsuarioPorEmailPort buscarUsuarioPorEmailPort,
            AlterarSenhaUsuarioPort alterarSenhaUsuarioPort) {
        this.cadastrarUsuarioPort = cadastrarUsuarioPort;
        this.buscarUsuarioPorEmailPort = buscarUsuarioPorEmailPort;
        this.alterarSenhaUsuarioPort = alterarSenhaUsuarioPort;
    }

    public void registerRoutes(Javalin app) {
        app.post("/usuarios", this::cadastrarUsuario);
        app.get("/usuarios/email/{email}", this::buscarPorEmail);
        app.patch("/usuarios/{id}/senha", this::alterarSenha);
    }

    // -------------------------------------------------------------------------
    // Request bodies (DTOs de entrada — apenas na infraestrutura)
    // -------------------------------------------------------------------------

    static class CadastrarUsuarioRequest {
        public String nome;
        public String email;
        public String senha;
        public String perfil;
    }

    static class AlterarSenhaRequest {
        public String novaSenha;
    }

    // -------------------------------------------------------------------------
    // Handlers
    // -------------------------------------------------------------------------

    private void cadastrarUsuario(Context ctx) {
        try {
            CadastrarUsuarioRequest request = ctx.bodyAsClass(CadastrarUsuarioRequest.class);

            Perfil perfil = Perfil.valueOf(request.perfil.toUpperCase());

            ComandoCadastrarUsuario comando = new ComandoCadastrarUsuario(
                    request.nome,
                    request.email,
                    request.senha,
                    perfil
            );

            Usuario usuarioCriado = cadastrarUsuarioPort.executar(comando);

            ctx.status(HttpStatus.CREATED).json(new UsuarioResponse(usuarioCriado));

        } catch (RegraNegocioException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result(e.getMessage());
        } catch (IllegalArgumentException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result("Perfil inválido. Valores aceitos: VISITANTE, PARTICIPANTE, ADMINISTRADOR.");
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Erro interno: " + e.getMessage());
        }
    }

    private void buscarPorEmail(Context ctx) {
        try {
            String email = ctx.pathParam("email");

            Usuario usuario = buscarUsuarioPorEmailPort.executar(email)
                    .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado com o e-mail: " + email));

            ctx.status(HttpStatus.OK).json(new UsuarioResponse(usuario));

        } catch (RegraNegocioException e) {
            ctx.status(HttpStatus.NOT_FOUND).result(e.getMessage());
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Erro interno: " + e.getMessage());
        }
    }

    private void alterarSenha(Context ctx) {
        try {
            Integer id = Integer.parseInt(ctx.pathParam("id"));

            AlterarSenhaRequest request = ctx.bodyAsClass(AlterarSenhaRequest.class);

            ComandoAlterarSenha comando = new ComandoAlterarSenha(id, request.novaSenha);

            Usuario usuarioAtualizado = alterarSenhaUsuarioPort.executar(comando);

            ctx.status(HttpStatus.OK).json(new UsuarioResponse(usuarioAtualizado));

        } catch (RegraNegocioException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result(e.getMessage());
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result("O id do usuário deve ser um número inteiro válido.");
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Erro interno: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Response body (DTO de saída — omite o hash da senha por segurança)
    // -------------------------------------------------------------------------

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
