package br.ueg.eventos.application.port.out;

public interface CertificadoRepositoryPort {
    void registrar(Long idEvento, Long idUsuario, int cargaHorariaMinutos, String arquivoNome);

    void registrarPessoaProgramacao(Long idEvento, Long idPessoa, String papel,
                                    int cargaHorariaMinutos, String arquivoNome);
}
