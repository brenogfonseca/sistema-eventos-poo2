package br.ueg.eventos.application.port.in.frequencia;

import br.ueg.eventos.domain.model.Frequencia;

import java.util.List;

public interface ListarFrequenciasPorInscricaoPort {

    List<Frequencia> executar(Long idInscricao);
}