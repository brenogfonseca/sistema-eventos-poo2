package br.ueg.eventos.application.port.in.inscricao;

import br.ueg.eventos.domain.model.Inscricao;

public interface InscreverUsuarioPort {

    Inscricao executar(Long idAtividade, Long idUsuario);
}