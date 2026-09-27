package br.ueg.eventos.application.port.in.resposta;

import br.ueg.eventos.domain.model.Resposta;

public interface AtualizarRespostaPort {
    class ComandoAtualizarResposta {
        public final Long id;
        public final String valor;

        public ComandoAtualizarResposta(Long id, String valor) {
            this.id = id;
            this.valor = valor;
        }
    }

    Resposta executar(ComandoAtualizarResposta comando);
}