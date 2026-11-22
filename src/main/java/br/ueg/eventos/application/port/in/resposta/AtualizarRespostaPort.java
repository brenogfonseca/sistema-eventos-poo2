package br.ueg.eventos.application.port.in.resposta;

import br.ueg.eventos.domain.model.Resposta;

public interface AtualizarRespostaPort {
    class ComandoAtualizarResposta {
        public final Long id;
        public final String valor;
        public final Integer idExecutor;

        public ComandoAtualizarResposta(Long id, String valor) {
            this(id, valor, null);
        }

        public ComandoAtualizarResposta(Long id, String valor, Integer idExecutor) {
            this.id = id;
            this.valor = valor;
            this.idExecutor = idExecutor;
        }
    }

    Resposta executar(ComandoAtualizarResposta comando);
}
