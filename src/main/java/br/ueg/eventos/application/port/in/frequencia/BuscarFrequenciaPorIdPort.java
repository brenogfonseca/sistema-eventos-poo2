package br.ueg.eventos.application.port.in.frequencia;

import br.ueg.eventos.domain.model.Frequencia;

public interface BuscarFrequenciaPorIdPort {

    Frequencia executar(Integer id);
}