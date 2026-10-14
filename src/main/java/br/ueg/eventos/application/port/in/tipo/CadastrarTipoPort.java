package br.ueg.eventos.application.port.in.tipo;

import br.ueg.eventos.domain.model.Tipo;

/** Define o caso de uso de cadastro de tipo. */
public interface CadastrarTipoPort {

    /** Dados necessários para cadastrar um tipo. */
    class ComandoCadastrarTipo {
        public final String nome;

        public ComandoCadastrarTipo(String nome) {
            this.nome = nome;
        }
    }

    Tipo executar(ComandoCadastrarTipo comando);
}