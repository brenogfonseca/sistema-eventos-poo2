package br.ueg.eventos.application.port.out;

import br.ueg.eventos.domain.model.Frequencia;

import java.util.List;
import java.util.Optional;

public interface FrequenciaRepositoryPort {

    Frequencia salvar(Frequencia frequencia);

    Optional<Frequencia> buscarPorId(Integer id);

    List<Frequencia> listarPorInscricao(Integer idInscricao);
}