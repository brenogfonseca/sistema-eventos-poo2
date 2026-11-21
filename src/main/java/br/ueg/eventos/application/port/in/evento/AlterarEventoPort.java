package br.ueg.eventos.application.port.in.evento;

import br.ueg.eventos.domain.model.Evento;
import br.ueg.eventos.domain.model.Usuario;

import java.time.LocalDateTime;

/**
 * Porta de entrada: caso de uso para alterar dados de um evento.
 * Exige a identificação do usuário executor para validação de segurança.
 */
public interface AlterarEventoPort {

    class ComandoAlterarEvento {
        public final Long idEvento;
        public final String titulo;
        public final String descricao;
        public final LocalDateTime inicio;
        public final LocalDateTime fim;
        public final int capacidade;
        public final Usuario usuarioExecutor; // Usuário executor da operação
        public final boolean certificado;
        public final int frequenciaMinima;
        public final boolean inscricoesAbertas;
        public final String modalidade;
        public final String status;
        public final String fusoHorario;
        public final LocalDateTime prazoCancelamento;

        public ComandoAlterarEvento(Long idEvento, String titulo, String descricao,
                                    LocalDateTime inicio, LocalDateTime fim, int capacidade,
                                    Usuario usuarioExecutor) {
            this(idEvento, titulo, descricao, inicio, fim, capacidade, usuarioExecutor, false, 75, false);
        }

        public ComandoAlterarEvento(Long idEvento, String titulo, String descricao,
                                    LocalDateTime inicio, LocalDateTime fim, int capacidade,
                                    Usuario usuarioExecutor, boolean certificado, int frequenciaMinima,
                                    boolean inscricoesAbertas) {
            this(idEvento, titulo, descricao, inicio, fim, capacidade, usuarioExecutor, certificado,
                    frequenciaMinima, inscricoesAbertas, "PRESENCIAL", "PUBLICADO", "America/Sao_Paulo");
        }

        public ComandoAlterarEvento(Long idEvento, String titulo, String descricao,
                                    LocalDateTime inicio, LocalDateTime fim, int capacidade,
                                    Usuario usuarioExecutor, boolean certificado, int frequenciaMinima,
                                    boolean inscricoesAbertas, String modalidade, String status, String fusoHorario) {
            this(idEvento, titulo, descricao, inicio, fim, capacidade, usuarioExecutor, certificado,
                    frequenciaMinima, inscricoesAbertas, modalidade, status, fusoHorario, null);
        }

        public ComandoAlterarEvento(Long idEvento, String titulo, String descricao,
                                    LocalDateTime inicio, LocalDateTime fim, int capacidade,
                                    Usuario usuarioExecutor, boolean certificado, int frequenciaMinima,
                                    boolean inscricoesAbertas, String modalidade, String status, String fusoHorario,
                                    LocalDateTime prazoCancelamento) {
            this.idEvento = idEvento;
            this.titulo = titulo;
            this.descricao = descricao;
            this.inicio = inicio;
            this.fim = fim;
            this.capacidade = capacidade;
            this.usuarioExecutor = usuarioExecutor;
            this.certificado = certificado;
            this.frequenciaMinima = frequenciaMinima;
            this.inscricoesAbertas = inscricoesAbertas;
            this.modalidade = modalidade;
            this.status = status;
            this.fusoHorario = fusoHorario;
            this.prazoCancelamento = prazoCancelamento;
        }
    }

    /**
     * Executa a alteração do evento após validação de segurança e regras de negócio.
     *
     * @param comando dados da alteração incluindo o executor
     * @return evento atualizado
     */
    Evento executar(ComandoAlterarEvento comando);
}
