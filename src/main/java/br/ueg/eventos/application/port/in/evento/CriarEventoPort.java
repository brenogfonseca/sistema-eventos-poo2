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
        public final Integer idUsuarioCriador; // ID do organizador/criador do evento
        public final boolean certificado;
        public final int frequenciaMinima;
        public final boolean inscricoesAbertas;
        public final String modalidade;
        public final String status;
        public final String fusoHorario;
        public final LocalDateTime prazoCancelamento;

        // Construtor original para compatibilidade
        public ComandoCriarEvento(String titulo, String descricao, LocalDateTime inicio, LocalDateTime fim, int capacidade) {
            this(titulo, descricao, inicio, fim, capacidade, null);
        }

        // Construtor completo incluindo o criador
        public ComandoCriarEvento(String titulo, String descricao, LocalDateTime inicio, LocalDateTime fim, int capacidade, Integer idUsuarioCriador) {
            this(titulo, descricao, inicio, fim, capacidade, idUsuarioCriador, false, 75, false);
        }

        public ComandoCriarEvento(String titulo, String descricao, LocalDateTime inicio, LocalDateTime fim,
                                  int capacidade, Integer idUsuarioCriador, boolean certificado, int frequenciaMinima,
                                  boolean inscricoesAbertas) {
            this(titulo, descricao, inicio, fim, capacidade, idUsuarioCriador, certificado,
                    frequenciaMinima, inscricoesAbertas, "PRESENCIAL", "PUBLICADO", "America/Sao_Paulo");
        }

        public ComandoCriarEvento(String titulo, String descricao, LocalDateTime inicio, LocalDateTime fim,
                                  int capacidade, Integer idUsuarioCriador, boolean certificado, int frequenciaMinima,
                                  boolean inscricoesAbertas, String modalidade, String status, String fusoHorario) {
            this(titulo, descricao, inicio, fim, capacidade, idUsuarioCriador, certificado, frequenciaMinima,
                    inscricoesAbertas, modalidade, status, fusoHorario, null);
        }

        public ComandoCriarEvento(String titulo, String descricao, LocalDateTime inicio, LocalDateTime fim,
                                  int capacidade, Integer idUsuarioCriador, boolean certificado, int frequenciaMinima,
                                  boolean inscricoesAbertas, String modalidade, String status, String fusoHorario,
                                  LocalDateTime prazoCancelamento) {
            this.titulo = titulo;
            this.descricao = descricao;
            this.inicio = inicio;
            this.fim = fim;
            this.capacidade = capacidade;
            this.idUsuarioCriador = idUsuarioCriador;
            this.certificado = certificado;
            this.frequenciaMinima = frequenciaMinima;
            this.inscricoesAbertas = inscricoesAbertas;
            this.modalidade = modalidade;
            this.status = status;
            this.fusoHorario = fusoHorario;
            this.prazoCancelamento = prazoCancelamento;
        }
    }

    Evento executar(ComandoCriarEvento comando);
}

