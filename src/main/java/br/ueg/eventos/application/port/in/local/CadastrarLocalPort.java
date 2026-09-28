package br.ueg.eventos.application.port.in.local;

import br.ueg.eventos.domain.model.Local;

/**
 * Porta de entrada: caso de uso para cadastrar um novo local no sistema.
 */
public interface CadastrarLocalPort {

    /**
     * Comando imutável com os dados necessários para cadastrar um local.
     * Segue o padrão Command Object para manter a interface explícita e independente da web.
     */
    class ComandoCadastrarLocal {
        public final String nome;

        public ComandoCadastrarLocal(String nome) {
            this.nome = nome;
        }
    }

    /**
     * Executa o caso de uso de cadastro de local.
     *
     * @param comando os dados do novo local
     * @return o local cadastrado com id gerado
     */
    Local executar(ComandoCadastrarLocal comando);
}
