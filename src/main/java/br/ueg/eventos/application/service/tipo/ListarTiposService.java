package br.ueg.eventos.application.service.tipo;

import br.ueg.eventos.application.port.in.tipo.ListarTiposPort;
import br.ueg.eventos.application.port.out.TipoRepositoryPort;
import br.ueg.eventos.domain.model.Tipo;

import java.util.List;

/** Serviço de listagem de tipos. */
public class ListarTiposService implements ListarTiposPort {

    private final TipoRepositoryPort tipoRepository;

    public ListarTiposService(TipoRepositoryPort tipoRepository) {
        this.tipoRepository = tipoRepository;
    }

    @Override
    public List<Tipo> executar() {
        return tipoRepository.listarTodos();
    }
}