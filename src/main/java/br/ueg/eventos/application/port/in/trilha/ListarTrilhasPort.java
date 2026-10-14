package br.ueg.eventos.application.port.in.trilha;

import br.ueg.eventos.domain.model.Trilha;

import java.util.List;

/** Define o caso de uso de listagem de trilhas. */
public interface ListarTrilhasPort {
    List<Trilha> executar();
}