package br.ueg.eventos.application.port.in.usuario;

import br.ueg.eventos.domain.model.Usuario;

import java.util.Optional;

/**
 * Porta de entrada: caso de uso para buscar um usuário pelo seu e-mail.
 */
public interface BuscarUsuarioPorEmailPort {

    /**
     * Busca um usuário pelo e-mail informado.
     *
     * @param email o e-mail do usuário a ser buscado
     * @return um Optional contendo o usuário, ou vazio se não encontrado
     */
    Optional<Usuario> executar(String email);
}
