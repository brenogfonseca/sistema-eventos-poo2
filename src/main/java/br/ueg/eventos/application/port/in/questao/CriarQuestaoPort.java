package br.ueg.eventos.application.port.in.questao;

import br.ueg.eventos.domain.model.Questao;
import br.ueg.eventos.domain.model.Questao.TipoQuestao;

public interface CriarQuestaoPort {
    class ComandoCriarQuestao {
        public final Long idQuestionario;
        public final String enunciado;
        public final TipoQuestao tipo;

        public ComandoCriarQuestao(Long idQuestionario, String enunciado, TipoQuestao tipo) {
            this.idQuestionario = idQuestionario;
            this.enunciado = enunciado;
            this.tipo = tipo;
        }
    }

    Questao executar(ComandoCriarQuestao comando);
}