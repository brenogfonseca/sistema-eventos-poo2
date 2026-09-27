package br.ueg.eventos.application.port.in.questionario;

import br.ueg.eventos.domain.model.Questionario;

public interface CriarQuestionarioPort {

    class ComandoCriarQuestionario {
        public final Long idAtividade;
        public final String titulo;

        public ComandoCriarQuestionario(Long idAtividade, String titulo) {
            this.idAtividade = idAtividade;
            this.titulo = titulo;
        }
    }

    Questionario executar(ComandoCriarQuestionario comando);
}