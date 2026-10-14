package br.ueg.eventos.application.port.in.tipo;

import br.ueg.eventos.domain.model.Tipo;

import java.util.List;

/** Define o caso de uso de listagem de tipos. */
public interface ListarTiposPort {
    List<Tipo> executar();
}