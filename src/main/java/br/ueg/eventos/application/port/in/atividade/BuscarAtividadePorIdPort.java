package br.ueg.eventos.application.port.in.atividade;

import br.ueg.eventos.domain.model.Atividade;

public interface BuscarAtividadePorIdPort {
    Atividade executar(Long id);
}