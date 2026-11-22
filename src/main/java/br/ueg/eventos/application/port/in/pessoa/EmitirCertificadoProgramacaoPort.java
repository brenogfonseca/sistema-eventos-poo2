package br.ueg.eventos.application.port.in.pessoa;

public interface EmitirCertificadoProgramacaoPort {
    Documento executar(Long idEvento, Long idPessoa, String papel);

    record Documento(String nomeArquivo, byte[] conteudo) { }
}
