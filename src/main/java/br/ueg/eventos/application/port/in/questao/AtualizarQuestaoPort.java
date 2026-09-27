package br.ueg.eventos.application.port.in.questao;

import br.ueg.eventos.domain.model.Questao;
import br.ueg.eventos.domain.model.Questao.TipoQuestao;

public interface AtualizarQuestaoPort {
    class ComandoAtualizarQuestao {
        public final Long id;
        public final String enunciado;
        public final TipoQuestao tipo;

        public ComandoAtualizarQuestao(Long id, String enunciado, TipoQuestao tipo) {
            this.id = id;
            this.enunciado = enunciado;
            this.tipo = tipo;
        }
    }

    Questao executar(ComandoAtualizarQuestao comando);
}