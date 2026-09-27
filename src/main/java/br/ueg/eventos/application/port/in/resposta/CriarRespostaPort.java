package br.ueg.eventos.application.port.in.resposta;

import br.ueg.eventos.domain.model.Resposta;

public interface CriarRespostaPort {
    class ComandoCriarResposta {
        public final Long idQuestao;
        public final Integer idUsuario;
        public final String valor;

        public ComandoCriarResposta(Long idQuestao, Integer idUsuario, String valor) {
            this.idQuestao = idQuestao;
            this.idUsuario = idUsuario;
            this.valor = valor;
        }
    }

    Resposta executar(ComandoCriarResposta comando);
}