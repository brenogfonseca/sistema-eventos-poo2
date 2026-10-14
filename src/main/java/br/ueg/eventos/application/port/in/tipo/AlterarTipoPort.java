package br.ueg.eventos.application.port.in.tipo;

import br.ueg.eventos.domain.model.Tipo;

/** Define o caso de uso de alteração de tipo. */
public interface AlterarTipoPort {

    /** Dados usados para alterar um tipo. */
    class ComandoAlterarTipo {
        public final Long id;
        public final String nome;

        public ComandoAlterarTipo(Long id, String nome) {
            this.id = id;
            this.nome = nome;
        }
    }

    Tipo executar(ComandoAlterarTipo comando);
}