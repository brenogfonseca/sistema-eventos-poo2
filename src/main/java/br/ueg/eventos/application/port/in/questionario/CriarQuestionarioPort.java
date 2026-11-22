package br.ueg.eventos.application.port.in.questionario;

import br.ueg.eventos.domain.model.Questionario;

public interface CriarQuestionarioPort {

    class ComandoCriarQuestionario {
        public final Long idAtividade;
        public final String titulo;
        public final boolean anonimo;
        public final boolean publicado;

        public ComandoCriarQuestionario(Long idAtividade, String titulo) {
            this(idAtividade, titulo, false, false);
        }

        public ComandoCriarQuestionario(Long idAtividade, String titulo, boolean anonimo, boolean publicado) {
            this.idAtividade = idAtividade;
            this.titulo = titulo;
            this.anonimo = anonimo;
            this.publicado = publicado;
        }
    }

    Questionario executar(ComandoCriarQuestionario comando);
}
