package br.ueg.eventos.application.service.evento;

import br.ueg.eventos.application.port.in.evento.BuscarVinculoOrganizadorPort;
import br.ueg.eventos.application.port.out.VinculoOrganizadorRepositoryPort;
import br.ueg.eventos.domain.model.VinculoOrganizador;

import java.util.List;

/**
 * Serviço de aplicação para consultar vínculos de organizadores.
 */
public class BuscarVinculoOrganizadorService implements BuscarVinculoOrganizadorPort {

    private final VinculoOrganizadorRepositoryPort vinculoOrganizadorRepository;

    public BuscarVinculoOrganizadorService(VinculoOrganizadorRepositoryPort vinculoOrganizadorRepository) {
        this.vinculoOrganizadorRepository = vinculoOrganizadorRepository;
    }

    @Override
    public boolean isOrganizador(Integer usuarioId, Integer eventoId) {
        if (usuarioId == null || eventoId == null) {
            return false;
        }
        return vinculoOrganizadorRepository.buscarPorUsuarioEEvento(usuarioId, eventoId).isPresent();
    }

    @Override
    public List<VinculoOrganizador> listarPorEvento(Integer eventoId) {
        if (eventoId == null) {
            return List.of();
        }
        return vinculoOrganizadorRepository.listarPorEvento(eventoId);
    }
}
