package br.ueg.eventos.application.port.out;

import br.ueg.eventos.domain.model.VinculoOrganizador;

import java.util.List;
import java.util.Optional;

/**
 * Porta de saída: contrato de repositório para persistência de vínculos de organizadores.
 */
public interface VinculoOrganizadorRepositoryPort {

    /**
     * Salva ou atualiza um vínculo de organizador.
     *
     * @param vinculo vínculo a ser persistido
     * @return vínculo persistido com ID gerado
     */
    VinculoOrganizador salvar(VinculoOrganizador vinculo);

    /**
     * Busca um vínculo específico por usuário e evento.
     *
     * @param usuarioId ID do usuário
     * @param eventoId ID do evento
     * @return Optional com o vínculo encontrado se existir
     */
    Optional<VinculoOrganizador> buscarPorUsuarioEEvento(Integer usuarioId, Integer eventoId);

    /**
     * Lista todos os vínculos de um determinado evento.
     *
     * @param eventoId ID do evento
     * @return lista de vínculos do evento
     */
    List<VinculoOrganizador> listarPorEvento(Integer eventoId);

    /**
     * Lista todos os vínculos de um determinado usuário.
     *
     * @param usuarioId ID do usuário
     * @return lista de vínculos do usuário
     */
    List<VinculoOrganizador> listarPorUsuario(Integer usuarioId);

    /**
     * Remove todos os vínculos associados a um evento (ex: ao excluir o evento).
     *
     * @param eventoId ID do evento
     */
    void deletarPorEvento(Integer eventoId);
}
