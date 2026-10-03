package br.ueg.eventos.application.port.in.questionario;

import br.ueg.eventos.domain.model.Questionario;

public interface AtualizarQuestionarioPort {

    class ComandoAtualizarQuestionario {
        public final Long id;
        public final String titulo;

        public ComandoAtualizarQuestionario(Long id, String titulo) {
            this.id = id;
            this.titulo = titulo;
        }
    }

    Questionario executar(ComandoAtualizarQuestionario comando);
}