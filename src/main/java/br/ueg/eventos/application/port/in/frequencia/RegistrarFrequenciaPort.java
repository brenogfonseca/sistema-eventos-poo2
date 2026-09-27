package br.ueg.eventos.application.port.in.frequencia;

import br.ueg.eventos.domain.model.Frequencia;

import java.time.LocalDateTime;

public interface RegistrarFrequenciaPort {

    class ComandoRegistrarFrequencia {
        public final LocalDateTime dataHora;
        public final String origem;
        public final Integer idResponsavel;
        public final boolean presente;
        public final Long idInscricao;
        public final String tipo;

        public ComandoRegistrarFrequencia(
                LocalDateTime dataHora,
                String origem,
                Integer idResponsavel,
                boolean presente,
                Long idInscricao,
                String tipo
        ) {
            this.dataHora = dataHora;
            this.origem = origem;
            this.idResponsavel = idResponsavel;
            this.presente = presente;
            this.idInscricao = idInscricao;
            this.tipo = tipo;
        }
    }

    Frequencia executar(ComandoRegistrarFrequencia comando);
}