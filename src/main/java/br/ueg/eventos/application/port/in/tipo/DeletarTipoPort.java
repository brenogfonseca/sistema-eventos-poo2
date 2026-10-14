package br.ueg.eventos.application.port.in.tipo;

/** Define o caso de uso de exclusão de tipo. */
public interface DeletarTipoPort {
    void executar(Long id);
}