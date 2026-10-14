package br.ueg.eventos.application.service.tipo;

import br.ueg.eventos.application.port.in.tipo.DeletarTipoPort;
import br.ueg.eventos.application.port.out.TipoRepositoryPort;
import br.ueg.eventos.domain.exception.RegraNegocioException;

/** Serviço de exclusão de tipo. */
public class DeletarTipoService implements DeletarTipoPort {

    private final TipoRepositoryPort tipoRepository;

    public DeletarTipoService(TipoRepositoryPort tipoRepository) {
        this.tipoRepository = tipoRepository;
    }

    @Override
    public void executar(Long id) {
        if (id == null || id <= 0) {
            throw new RegraNegocioException("ID do tipo inválido.");
        }
        if (tipoRepository.buscarPorId(id).isEmpty()) {
            throw new RegraNegocioException("Tipo não encontrado.");
        }

        tipoRepository.deletar(id);
    }
}