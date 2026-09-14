package br.ueg.eventos.application.port.in.evento;

import br.ueg.eventos.domain.model.Evento;
import java.time.LocalDateTime;

public interface CriarEventoPort {

    class ComandoCriarEvento {
        public final String titulo;
        public final String descricao;
        public final LocalDateTime inicio;
        public final LocalDateTime fim;
        public final int capacidade;

        public ComandoCriarEvento(String titulo, String descricao, LocalDateTime inicio, LocalDateTime fim, int capacidade) {
            this.titulo = titulo;
            this.descricao = descricao;
            this.inicio = inicio;
            this.fim = fim;
            this.capacidade = capacidade;
        }
    }

    Evento executar(ComandoCriarEvento comando);
}
