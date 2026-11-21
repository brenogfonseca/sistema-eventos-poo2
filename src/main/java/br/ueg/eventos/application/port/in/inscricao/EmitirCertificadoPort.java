package br.ueg.eventos.application.port.in.inscricao;

public interface EmitirCertificadoPort {
    Documento executar(Long idInscricaoEvento, Long idUsuario);

    record Documento(String nomeArquivo, byte[] conteudo) { }
}
