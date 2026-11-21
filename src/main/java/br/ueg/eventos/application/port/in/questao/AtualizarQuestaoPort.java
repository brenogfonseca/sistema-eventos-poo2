package br.ueg.eventos.application.port.in.questao;

import br.ueg.eventos.domain.model.Questao;
import br.ueg.eventos.domain.model.Questao.TipoQuestao;
import java.util.List;

public interface AtualizarQuestaoPort {
    class ComandoAtualizarQuestao {
        public final Long id;
        public final String enunciado;
        public final TipoQuestao tipo;
        public final List<String> opcoes;
        public final Integer escalaMinima;
        public final Integer escalaMaxima;

        public ComandoAtualizarQuestao(Long id, String enunciado, TipoQuestao tipo) {
            this(id, enunciado, tipo, null, null, null);
        }

        public ComandoAtualizarQuestao(Long id, String enunciado, TipoQuestao tipo,
                                       List<String> opcoes, Integer escalaMinima, Integer escalaMaxima) {
            this.id = id;
            this.enunciado = enunciado;
            this.tipo = tipo;
            this.opcoes = opcoes == null ? List.of() : List.copyOf(opcoes);
            this.escalaMinima = escalaMinima;
            this.escalaMaxima = escalaMaxima;
        }
    }

    Questao executar(ComandoAtualizarQuestao comando);
}
