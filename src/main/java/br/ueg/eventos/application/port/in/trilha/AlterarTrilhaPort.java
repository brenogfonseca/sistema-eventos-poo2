package br.ueg.eventos.application.port.in.trilha;

import br.ueg.eventos.domain.model.Trilha;

/** Define o caso de uso de alteração de trilha. */
public interface AlterarTrilhaPort {

    /** Dados usados para alterar uma trilha. */
    class ComandoAlterarTrilha {
        public final Long id;
        public final String nome;

        public ComandoAlterarTrilha(Long id, String nome) {
            this.id = id;
            this.nome = nome;
        }
    }

    Trilha executar(ComandoAlterarTrilha comando);
}