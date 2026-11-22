package br.ueg.eventos.domain.model;

import br.ueg.eventos.domain.exception.RegraNegocioException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventoTest {
    private Evento novoEvento() {
        return new Evento("Evento", "Descrição",
                new Periodo(LocalDateTime.parse("2027-04-10T08:00:00"),
                        LocalDateTime.parse("2027-04-10T18:00:00")), 100);
    }

    @Test
    void aceitaSomenteOsTresLimitesDeFrequenciaDefinidos() {
        for (int minimo : new int[]{50, 75, 100}) {
            Evento evento = novoEvento();
            evento.habilitarCertificado(minimo);
            assertTrue(evento.isCertificado());
            assertEquals(minimo, evento.getFrequenciaMinima());
        }
    }

    @Test
    void rejeitaFrequenciaMinimaForaDaPolitica() {
        assertThrows(RegraNegocioException.class, () -> novoEvento().habilitarCertificado(80));
    }

    @Test
    void rascunhoNaoMantemInscricoesAbertas() {
        Evento evento = novoEvento();
        evento.abrirInscricoes();
        evento.configurarPublicacao("PRESENCIAL", "RASCUNHO", "America/Sao_Paulo");
        assertFalse(evento.isInscricoesAbertas());
    }
}
