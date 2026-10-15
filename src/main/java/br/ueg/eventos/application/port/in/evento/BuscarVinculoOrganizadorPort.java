package br.ueg.eventos.application.port.in.evento;

import br.ueg.eventos.domain.model.VinculoOrganizador;

import java.util.List;

/**
 * Porta de entrada: consulta de vínculos de organizador para uso nos controllers e regras de negócio.
 */
public interface BuscarVinculoOrganizadorPort {

    /**
     * Verifica se um usuário possui vínculo de organizador ativo (criador) com o evento.
     *
     * @param usuarioId ID do usuário
     * @param eventoId ID do evento
     * @return true se o usuário for organizador/criador do evento
     */
    boolean isOrganizador(Integer usuarioId, Integer eventoId);

    /**
     * Lista todos os vínculos de um evento.
     *
     * @param eventoId ID do evento
     * @return lista de vínculos de organizadores
     */
    List<VinculoOrganizador> listarPorEvento(Integer eventoId);
}
