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

        public ComandoAlterarEvento(Long idEvento, String titulo, String descricao,
                                    LocalDateTime inicio, LocalDateTime fim, int capacidade,
                                    Usuario usuarioExecutor) {
            this.idEvento = idEvento;
            this.titulo = titulo;
            this.descricao = descricao;
            this.inicio = inicio;
            this.fim = fim;
            this.capacidade = capacidade;
            this.usuarioExecutor = usuarioExecutor;
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
