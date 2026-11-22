package br.ueg.eventos.application.port.out;

import java.util.List;

/** Permissões de portaria limitadas às atividades atribuídas ao colaborador. */
public interface VinculoColaboradorRepositoryPort {
    void vincular(Integer idUsuario, Long idAtividade);
    void remover(Integer idUsuario, Long idAtividade);
    boolean estaVinculado(Integer idUsuario, Long idAtividade);
    List<Integer> listarUsuariosPorAtividade(Long idAtividade);
    List<Long> listarAtividadesPorUsuario(Integer idUsuario);
}
