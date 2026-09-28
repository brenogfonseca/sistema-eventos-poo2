package br.ueg.eventos.application.port.in.local;

import br.ueg.eventos.domain.model.Local;

import java.util.List;

/**
 * Porta de entrada: caso de uso para listar todos os locais cadastrados.
 */
public interface ListarLocaisPort {

    /**
     * Executa o caso de uso de listagem de locais.
     *
     * @return lista contendo todos os locais
     */
    List<Local> executar();
}
