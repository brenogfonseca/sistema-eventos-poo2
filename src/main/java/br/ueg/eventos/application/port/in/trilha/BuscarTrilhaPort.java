package br.ueg.eventos.application.port.in.trilha;

import br.ueg.eventos.domain.model.Trilha;

/** Define o caso de uso de busca de trilha por ID. */
public interface BuscarTrilhaPort {
    Trilha executar(Long id);
}