package br.ueg.eventos.application.port.in.frequencia;

import br.ueg.eventos.domain.model.Frequencia;

public interface AtualizarPresencaPort {

    Frequencia executar(Integer idFrequencia, boolean presente);
}