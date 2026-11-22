package br.ueg.eventos.application.port.out;

import java.time.Instant;

/** Signs and validates short-lived credentials for API clients. */
public interface AuthenticationTokenPort {
    Token emitir(Integer idUsuario, String perfil);
    Principal validar(String token);

    record Token(String valor, Instant expiraEm) { }
    record Principal(Integer idUsuario, String perfil, Instant expiraEm) { }
}
