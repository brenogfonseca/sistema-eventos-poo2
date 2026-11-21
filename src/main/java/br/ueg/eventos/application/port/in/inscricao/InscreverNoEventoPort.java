package br.ueg.eventos.application.port.in.inscricao;

import br.ueg.eventos.domain.model.InscricaoEvento;

public interface InscreverNoEventoPort {
    InscricaoEvento executar(Long idEvento, Long idUsuario);
}
