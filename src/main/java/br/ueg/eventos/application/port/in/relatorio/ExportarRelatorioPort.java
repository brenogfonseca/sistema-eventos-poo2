package br.ueg.eventos.application.port.in.relatorio;

public interface ExportarRelatorioPort {
    String inscricoesCsv(Long idEvento, Integer idExecutor);
    String frequenciasCsv(Long idEvento, Integer idExecutor);
}
