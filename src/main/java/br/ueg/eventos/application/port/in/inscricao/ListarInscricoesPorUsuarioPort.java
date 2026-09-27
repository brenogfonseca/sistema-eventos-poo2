package br.ueg.eventos.application.port.in.inscricao;

import br.ueg.eventos.domain.model.Inscricao;

import java.util.List;

public interface ListarInscricoesPorUsuarioPort {

    List<Inscricao> executar(Long idUsuario);
}