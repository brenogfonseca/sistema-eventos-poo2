package br.ueg.eventos.application.port.in.inscricao;

import br.ueg.eventos.domain.model.Inscricao;

import java.util.List;

public interface ListarInscricoesPorAtividadePort {

    List<Inscricao> executar(Long idAtividade);
}   