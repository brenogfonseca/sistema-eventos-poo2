package br.ueg.eventos.application.port.in.atividade;

import br.ueg.eventos.domain.model.Atividade;

public interface AtualizarAtividadePort {
    Atividade executar(Long id, Atividade atividade);
}