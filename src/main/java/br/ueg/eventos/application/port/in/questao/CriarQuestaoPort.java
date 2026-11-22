package br.ueg.eventos.application.port.in.questao;

import br.ueg.eventos.domain.model.Questao;
import br.ueg.eventos.domain.model.Questao.TipoQuestao;
import java.util.List;

public interface CriarQuestaoPort {
    class ComandoCriarQuestao {
        public final Long idQuestionario;
        public final String enunciado;
        public final TipoQuestao tipo;
        public final List<String> opcoes;
        public final Integer escalaMinima;
        public final Integer escalaMaxima;

        public ComandoCriarQuestao(Long idQuestionario, String enunciado, TipoQuestao tipo) {
            this(idQuestionario, enunciado, tipo, null, null, null);
        }

        public ComandoCriarQuestao(Long idQuestionario, String enunciado, TipoQuestao tipo,
                                   List<String> opcoes, Integer escalaMinima, Integer escalaMaxima) {
            this.idQuestionario = idQuestionario;
            this.enunciado = enunciado;
            this.tipo = tipo;
            this.opcoes = opcoes == null ? List.of() : List.copyOf(opcoes);
            this.escalaMinima = escalaMinima;
            this.escalaMaxima = escalaMaxima;
        }
    }

    Questao executar(ComandoCriarQuestao comando);
}
