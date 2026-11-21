package br.ueg.eventos.infrastructure.adapter.out.pdf;

import br.ueg.eventos.application.port.out.CertificadoPdfGeneratorPort;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Minimal one-page PDF writer for printable participant certificates. */
public final class SimpleCertificatePdfGenerator implements CertificadoPdfGeneratorPort {
    private static final Charset WIN_ANSI = Charset.forName("windows-1252");
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public byte[] gerar(String participante, String evento, double frequenciaPercentual,
                        int frequenciaMinima, int cargaHorariaMinutos, LocalDateTime emitidoEm) {
        String percentual = String.format(Locale.forLanguageTag("pt-BR"), "%.2f%%", frequenciaPercentual);
        String resumo = "com frequência de " + percentual + " (mínimo " + frequenciaMinima
                + "%) e carga horária de " + cargaHoraria(cargaHorariaMinutos) + ".";
        return gerarDocumento(participante, evento, "participou do evento", resumo, emitidoEm);
    }

    @Override
    public byte[] gerarProgramacao(String nome, String evento, String papel,
                                   int cargaHorariaMinutos, LocalDateTime emitidoEm) {
        String papelLegivel = "PALESTRANTE".equals(papel) ? "palestrante" : "apresentador(a)";
        String resumo = "com carga horária de " + cargaHoraria(cargaHorariaMinutos) + ".";
        return gerarDocumento(nome, evento, "atuou como " + papelLegivel + " no evento", resumo, emitidoEm);
    }

    private byte[] gerarDocumento(String titular, String evento, String participacao,
                                  String resumo, LocalDateTime emitidoEm) {
        String nome = limitar(titular, 100);
        List<String> linhasEvento = quebrar(limitar(evento, 150), 72);

        StringBuilder pagina = new StringBuilder();
        pagina.append("0.22 0.36 0.54 RG 2 w 32 32 778 531 re S\n");
        pagina.append("0.83 0.65 0.25 RG 1 w 45 45 752 505 re S\n");
        texto(pagina, "F2", 14, 250, 520, "UNIVERSIDADE ESTADUAL DE GOIÁS");
        texto(pagina, "F2", 30, 325, 455, "CERTIFICADO");
        texto(pagina, "F1", 16, 340, 395, "Certificamos que");
        texto(pagina, "F2", 24, 96, 350, nome);
        texto(pagina, "F1", 16, 130, 310, limitar(participacao, 72));
        int yEvento = 270;
        for (String linha : linhasEvento) {
            texto(pagina, "F2", 18, 78, yEvento, linha);
            yEvento -= 26;
        }
        int yResumo = Math.max(150, yEvento - 30);
        texto(pagina, "F1", 14, 95, yResumo, limitar(resumo, 90));
        texto(pagina, "F1", 12, 95, 95, "Emitido em " + emitidoEm.format(DATA) + ".");
        pagina.append("0.22 0.36 0.54 RG 1 w 500 115 m 740 115 l S\n");
        texto(pagina, "F1", 10, 550, 96, "Coordenação do evento");

        byte[] stream = pagina.toString().getBytes(StandardCharsets.US_ASCII);
        List<byte[]> objetos = List.of(
                ascii("<< /Type /Catalog /Pages 2 0 R >>"),
                ascii("<< /Type /Pages /Kids [3 0 R] /Count 1 >>"),
                ascii("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 842 595] /Resources << /Font << /F1 4 0 R /F2 5 0 R >> >> /Contents 6 0 R >>"),
                ascii("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>"),
                ascii("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold /Encoding /WinAnsiEncoding >>"),
                stream
        );
        ByteArrayOutputStream pdf = new ByteArrayOutputStream();
        write(pdf, "%PDF-1.4\n");
        List<Integer> offsets = new ArrayList<>();
        for (int i = 0; i < objetos.size(); i++) {
            offsets.add(pdf.size());
            write(pdf, (i + 1) + " 0 obj\n");
            if (i == 5) {
                write(pdf, "<< /Length " + objetos.get(i).length + " >>\nstream\n");
                pdf.writeBytes(objetos.get(i));
                write(pdf, "\nendstream\nendobj\n");
            } else {
                pdf.writeBytes(objetos.get(i));
                write(pdf, "\nendobj\n");
            }
        }
        int xref = pdf.size();
        write(pdf, "xref\n0 " + (objetos.size() + 1) + "\n0000000000 65535 f \n");
        for (int offset : offsets) write(pdf, String.format(Locale.ROOT, "%010d 00000 n \n", offset));
        write(pdf, "trailer\n<< /Size " + (objetos.size() + 1) + " /Root 1 0 R >>\nstartxref\n" + xref + "\n%%EOF\n");
        return pdf.toByteArray();
    }

    private String cargaHoraria(int cargaHorariaMinutos) {
        int horas = cargaHorariaMinutos / 60;
        int minutos = cargaHorariaMinutos % 60;
        return horas + " h" + (minutos == 0 ? "" : " " + minutos + " min");
    }

    private void texto(StringBuilder pagina, String fonte, int tamanho, int x, int y, String valor) {
        byte[] bytes = valor.getBytes(WIN_ANSI);
        StringBuilder hex = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) hex.append(String.format(Locale.ROOT, "%02X", b & 0xff));
        pagina.append("BT /").append(fonte).append(' ').append(tamanho).append(" Tf ")
                .append(x).append(' ').append(y).append(" Td <").append(hex).append("> Tj ET\n");
    }

    private List<String> quebrar(String valor, int limite) {
        List<String> linhas = new ArrayList<>();
        StringBuilder linha = new StringBuilder();
        for (String palavra : valor.split("\\s+")) {
            if (linha.length() > 0 && linha.length() + palavra.length() + 1 > limite) {
                linhas.add(linha.toString());
                linha.setLength(0);
            }
            if (linha.length() > 0) linha.append(' ');
            linha.append(palavra);
        }
        if (linha.length() > 0) linhas.add(linha.toString());
        return linhas.isEmpty() ? List.of("") : linhas;
    }

    private String limitar(String texto, int maximo) {
        if (texto == null) return "";
        String seguro = texto.replaceAll("[\\p{Cntrl}]", " ").trim();
        return seguro.length() <= maximo ? seguro : seguro.substring(0, maximo);
    }

    private byte[] ascii(String valor) { return valor.getBytes(StandardCharsets.US_ASCII); }
    private void write(ByteArrayOutputStream destino, String valor) { destino.writeBytes(ascii(valor)); }
}
