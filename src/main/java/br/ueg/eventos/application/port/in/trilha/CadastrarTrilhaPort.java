package br.ueg.eventos.application.port.in.trilha;

import br.ueg.eventos.domain.model.Trilha;

/** Define o caso de uso de cadastro de trilha. */
public interface CadastrarTrilhaPort {

    /** Dados necessários para cadastrar uma trilha. */
    class ComandoCadastrarTrilha {
        public final String nome;

        public ComandoCadastrarTrilha(String nome) {
            this.nome = nome;
        }
    }

    Trilha executar(ComandoCadastrarTrilha comando);
}