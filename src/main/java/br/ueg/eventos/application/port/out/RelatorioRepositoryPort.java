package br.ueg.eventos.application.port.out;

public interface RelatorioRepositoryPort {
    String exportarInscricoesCsv(Long idEvento);
    String exportarFrequenciasCsv(Long idEvento);
}
