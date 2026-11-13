package br.ueg.eventos.application.port.in.inscricao;

public interface CancelarInscricaoEventoPort {
    void executar(Long idInscricaoEvento, Long idUsuario);
}
