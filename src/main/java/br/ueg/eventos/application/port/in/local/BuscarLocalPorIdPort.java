package br.ueg.eventos.application.port.in.local;

import br.ueg.eventos.domain.model.Local;

/**
 * Porta de entrada: caso de uso para buscar um local específico pelo seu ID.
 */
public interface BuscarLocalPorIdPort {

    /**
     * Executa o caso de uso de busca de local.
     *
     * @param id identificador do local desejado
     * @return o local encontrado
     */
    Local executar(Long id);
}
