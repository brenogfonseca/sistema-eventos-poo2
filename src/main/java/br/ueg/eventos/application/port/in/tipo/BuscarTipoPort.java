package br.ueg.eventos.application.port.in.tipo;

import br.ueg.eventos.domain.model.Tipo;

/** Define o caso de uso de busca de tipo por ID. */
public interface BuscarTipoPort {
    Tipo executar(Long id);
}