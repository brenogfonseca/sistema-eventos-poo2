package br.ueg.eventos.application.port.in.usuario;

import java.time.Instant;

public interface AutenticarUsuarioPort {
    DadosAutenticacao executar(String email, String senha);

    record DadosAutenticacao(Integer id, String nome, String email, String perfil,
                             String token, Instant expiraEm) { }
}
