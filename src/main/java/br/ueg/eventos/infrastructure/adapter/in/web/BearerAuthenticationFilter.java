package br.ueg.eventos.infrastructure.adapter.in.web;

import br.ueg.eventos.application.port.out.AuthenticationTokenPort;
import br.ueg.eventos.application.port.out.UsuarioRepositoryPort;
import br.ueg.eventos.domain.model.Usuario;
import io.javalin.Javalin;
import io.javalin.http.HttpStatus;

import java.util.Optional;

/** Enforces a signed bearer session for all non-public API routes. */
public final class BearerAuthenticationFilter {
    public static final String ATTRIBUTE_USER_ID = "authenticatedUserId";
    public static final String ATTRIBUTE_USER_ROLE = "authenticatedUserRole";
    private final AuthenticationTokenPort tokens;
    private final UsuarioRepositoryPort usuarios;

    public BearerAuthenticationFilter(AuthenticationTokenPort tokens, UsuarioRepositoryPort usuarios) {
        this.tokens = tokens;
        this.usuarios = usuarios;
    }

    public void install(Javalin app) {
        app.before(ctx -> {
            boolean publica = publicRoute(ctx.method().name(), ctx.path());
            String authorization = ctx.header("Authorization");
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                if (publica) return;
                ctx.status(HttpStatus.UNAUTHORIZED).result("Autenticação bearer obrigatória.");
                ctx.skipRemainingHandlers();
                return;
            }
            try {
                AuthenticationTokenPort.Principal principal =
                        tokens.validar(authorization.substring("Bearer ".length()).trim());
                Optional<Usuario> usuario = usuarios.buscarPorId(principal.idUsuario());
                if (usuario.isEmpty()) throw new IllegalArgumentException("Usuário não encontrado.");
                if (!usuario.get().getPerfil().name().equals(principal.perfil())) {
                    throw new IllegalArgumentException("Perfil da sessão desatualizado.");
                }
                String idInformado = ctx.header("X-Usuario-Id");
                if (idInformado == null || idInformado.isBlank()
                        || Integer.parseInt(idInformado) != principal.idUsuario()) {
                    ctx.status(HttpStatus.FORBIDDEN).result("O cabeçalho de usuário não corresponde à sessão.");
                    ctx.skipRemainingHandlers();
                    return;
                }
                ctx.attribute(ATTRIBUTE_USER_ID, principal.idUsuario());
                ctx.attribute(ATTRIBUTE_USER_ROLE, principal.perfil());
                if (rotaExigeAdministrador(ctx.method().name(), ctx.path())
                        && usuario.get().getPerfil() != br.ueg.eventos.domain.model.Perfil.ADMINISTRADOR) {
                    ctx.status(HttpStatus.FORBIDDEN).result("Esta operação exige perfil de administrador.");
                    ctx.skipRemainingHandlers();
                    return;
                }
            } catch (Exception e) {
                ctx.status(HttpStatus.UNAUTHORIZED).result("Sessão inválida ou expirada.");
                ctx.skipRemainingHandlers();
            }
        });
    }

    private boolean rotaExigeAdministrador(String method, String path) {
        return ("POST".equals(method) && "/locais".equals(path))
                || (("PUT".equals(method) || "DELETE".equals(method))
                && path.matches("/locais/[0-9]+"));
    }

    private boolean publicRoute(String method, String path) {
        if ("POST".equals(method) && ("/auth/login".equals(path) || "/usuarios".equals(path))) return true;
        if (!"GET".equals(method)) return false;
        return "/eventos".equals(path)
                || path.matches("/eventos/[0-9]+")
                || path.matches("/atividades/[0-9]+")
                || path.matches("/atividades/evento/[0-9]+")
                || path.matches("/atividades/[0-9]+/pessoas")
                || path.matches("/eventos/[0-9]+/pessoas-programacao");
    }
}
