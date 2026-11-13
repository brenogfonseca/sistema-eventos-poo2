package br.ueg.eventos.application.port.out;

import java.time.Instant;

/** Assina e valida os tokens do QR Code da inscrição no evento. */
public interface CheckinQrTokenSignerPort {

    String emitir(Long idInscricaoEvento, Instant expiraEm);

    DadosToken validar(String token);

    final class DadosToken {
        private final Long idInscricaoEvento;
        private final Instant expiraEm;

        public DadosToken(Long idInscricaoEvento, Instant expiraEm) {
            this.idInscricaoEvento = idInscricaoEvento;
            this.expiraEm = expiraEm;
        }

        public Long getIdInscricaoEvento() {
            return idInscricaoEvento;
        }

        public Instant getExpiraEm() {
            return expiraEm;
        }
    }
}
