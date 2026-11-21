package br.ueg.eventos.infrastructure.adapter.out.pdf;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SimpleCertificatePdfGeneratorTest {
    @Test
    void criaDocumentoPdfBaixavelComMarcaDeFim() {
        byte[] pdf = new SimpleCertificatePdfGenerator().gerar(
                "Ana Silva", "Jornada de Tecnologia", 87.5, 75, 255,
                LocalDateTime.parse("2027-04-10T18:05:00"));
        String conteudo = new String(pdf, StandardCharsets.US_ASCII);

        assertTrue(conteudo.startsWith("%PDF-1.4"));
        assertTrue(conteudo.contains("/Type /Page"));
        assertTrue(conteudo.contains("%%EOF"));
        assertTrue(pdf.length > 500);
    }
}
