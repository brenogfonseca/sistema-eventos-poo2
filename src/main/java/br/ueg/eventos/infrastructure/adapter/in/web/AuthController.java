package br.ueg.eventos.infrastructure.adapter.in.web;

import br.ueg.eventos.application.port.in.usuario.AutenticarUsuarioPort;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

public final class AuthController {
    private final AutenticarUsuarioPort autenticar;

    public AuthController(AutenticarUsuarioPort autenticar) {
        this.autenticar = autenticar;
    }

    public void registerRoutes(Javalin app) {
        app.post("/auth/login", this::login);
    }

    public static final class LoginRequest {
        public String email;
        public String senha;
    }

    private void login(Context ctx) {
        try {
            LoginRequest request = ctx.bodyAsClass(LoginRequest.class);
            var resultado = autenticar.executar(request.email, request.senha);
            ctx.status(HttpStatus.OK).json(resultado);
        } catch (RegraNegocioException e) {
            ctx.status(HttpStatus.UNAUTHORIZED).result(e.getMessage());
        } catch (IllegalStateException e) {
            ctx.status(HttpStatus.SERVICE_UNAVAILABLE).result("A autenticação do sistema não está configurada.");
        } catch (Exception e) {
            ctx.status(HttpStatus.BAD_REQUEST).result("Envie um JSON válido com e-mail e senha.");
        }
    }
}
