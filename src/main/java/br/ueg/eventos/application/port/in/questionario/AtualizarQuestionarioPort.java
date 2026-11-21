package br.ueg.eventos.application.port.in.questionario;

import br.ueg.eventos.domain.model.Questionario;

public interface AtualizarQuestionarioPort {

    class ComandoAtualizarQuestionario {
        public final Long id;
        public final String titulo;
        public final boolean anonimo;
        public final boolean publicado;

        public ComandoAtualizarQuestionario(Long id, String titulo) {
            this(id, titulo, false, false);
        }

        public ComandoAtualizarQuestionario(Long id, String titulo, boolean anonimo, boolean publicado) {
            this.id = id;
            this.titulo = titulo;
            this.anonimo = anonimo;
            this.publicado = publicado;
        }
    }

    Questionario executar(ComandoAtualizarQuestionario comando);
}
