package br.ueg.eventos.application.port.out;

import java.time.LocalDateTime;

public interface CertificadoPdfGeneratorPort {
    byte[] gerar(String participante, String evento, double frequenciaPercentual,
                 int frequenciaMinima, int cargaHorariaMinutos, LocalDateTime emitidoEm);

    byte[] gerarProgramacao(String nome, String evento, String papel,
                            int cargaHorariaMinutos, LocalDateTime emitidoEm);
}
