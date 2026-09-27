package br.ueg.eventos.application.port.in.usuario;

import br.ueg.eventos.domain.model.Usuario;

/**
 * Porta de entrada: caso de uso para buscar um usuário pelo seu id.
 * Acesso permitido ao próprio usuário ou a um administrador.
 */
public interface BuscarUsuarioPorIdPort {

    /**
     * Comando com o id do executor e o id do usuário alvo da busca.
     */
    class ComandoBuscarUsuarioPorId {
        public final Integer idExecutor;
        public final Integer idAlvo;

        public ComandoBuscarUsuarioPorId(Integer idExecutor, Integer idAlvo) {
            this.idExecutor = idExecutor;
            this.idAlvo = idAlvo;
        }
    }

    /**
     * Busca um usuário pelo id. ADMINISTRADOR acessa qualquer usuário;
     * USUARIO acessa apenas seus próprios dados.
     *
     * @param comando identificação do executor e do alvo
     * @return o usuário encontrado
     */
    Usuario executar(ComandoBuscarUsuarioPorId comando);
}
