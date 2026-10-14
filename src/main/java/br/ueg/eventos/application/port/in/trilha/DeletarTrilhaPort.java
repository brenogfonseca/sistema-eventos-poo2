package br.ueg.eventos.application.port.in.trilha;

/** Define o caso de uso de exclusão de trilha. */
public interface DeletarTrilhaPort {
    void executar(Long id);
}