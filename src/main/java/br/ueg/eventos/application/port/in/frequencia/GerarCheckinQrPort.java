package br.ueg.eventos.application.port.in.frequencia;

import java.time.Instant;

public interface GerarCheckinQrPort {

    DadosCheckinQr executar(Long idInscricao, Integer idUsuario);

    final class DadosCheckinQr {
        private final String token;
        private final Instant expiraEm;

        public DadosCheckinQr(String token, Instant expiraEm) {
            this.token = token;
            this.expiraEm = expiraEm;
        }

        public String getToken() {
            return token;
        }

        public Instant getExpiraEm() {
            return expiraEm;
        }
    }
}
