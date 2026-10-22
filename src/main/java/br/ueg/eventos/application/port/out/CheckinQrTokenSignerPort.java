package br.ueg.eventos.application.port.out;

import java.time.Instant;

/** Assina e valida tokens curtos usados nos QR Codes de check-in. */
public interface CheckinQrTokenSignerPort {

    String emitir(Long idInscricao, Instant expiraEm);

    DadosToken validar(String token);

    final class DadosToken {
        private final Long idInscricao;
        private final Instant expiraEm;

        public DadosToken(Long idInscricao, Instant expiraEm) {
            this.idInscricao = idInscricao;
            this.expiraEm = expiraEm;
        }

        public Long getIdInscricao() {
            return idInscricao;
        }

        public Instant getExpiraEm() {
            return expiraEm;
        }
    }
}
