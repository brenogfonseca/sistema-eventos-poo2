package br.ueg.eventos.domain.model;

import br.ueg.eventos.domain.exception.RegraNegocioException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PeriodoTest {
    @Test
    void identificaSobreposicaoEPermiteAtividadesAdjacentes() {
        Periodo primeiro = new Periodo(LocalDateTime.parse("2027-04-10T09:00:00"),
                LocalDateTime.parse("2027-04-10T10:00:00"));
        Periodo sobreposto = new Periodo(LocalDateTime.parse("2027-04-10T09:30:00"),
                LocalDateTime.parse("2027-04-10T10:30:00"));
        Periodo seguinte = new Periodo(LocalDateTime.parse("2027-04-10T10:00:00"),
                LocalDateTime.parse("2027-04-10T11:00:00"));

        assertTrue(primeiro.conflitaCom(sobreposto));
        assertFalse(primeiro.conflitaCom(seguinte));
    }

    @Test
    void rejeitaPeriodoInvertido() {
        assertThrows(RegraNegocioException.class, () -> new Periodo(
                LocalDateTime.parse("2027-04-10T10:00:00"),
                LocalDateTime.parse("2027-04-10T09:00:00")));
    }
}
