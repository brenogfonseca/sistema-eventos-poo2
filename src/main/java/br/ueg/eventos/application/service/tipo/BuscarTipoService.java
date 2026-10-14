package br.ueg.eventos.application.service.tipo;

import br.ueg.eventos.application.port.in.tipo.BuscarTipoPort;
import br.ueg.eventos.application.port.out.TipoRepositoryPort;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Tipo;

/** Serviço de busca de tipo por ID. */
public class BuscarTipoService implements BuscarTipoPort {

    private final TipoRepositoryPort tipoRepository;

    public BuscarTipoService(TipoRepositoryPort tipoRepository) {
        this.tipoRepository = tipoRepository;
    }

    @Override
    public Tipo executar(Long id) {
        if (id == null || id <= 0) {
            throw new RegraNegocioException("ID do tipo inválido.");
        }

        return tipoRepository.buscarPorId(id)
                .orElseThrow(() ->
                        new RegraNegocioException("Tipo não encontrado."));
    }
}