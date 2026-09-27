package br.ueg.eventos.application.port.in.inscricao;

import br.ueg.eventos.domain.model.Inscricao;

public interface BuscarInscricaoPorIdPort {

    Inscricao executar(Long idInscricao);
}