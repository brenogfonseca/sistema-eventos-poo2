package br.ueg.eventos.domain.model;

import br.ueg.eventos.domain.exception.RegraNegocioException;

/**
 * Entidade associativa que vincula um Usuário (organizador) a um Evento.
 * Demonstra imutabilidade de seus identificadores essenciais e encapsula o papel
 * do organizador (ex: Criador/Dono Principal).
 */
public class VinculoOrganizador {

    // Identificador único do vínculo (pode ser nulo antes de persistido)
    private Long id;

    // Identificador do usuário associado (imutável)
    private final Integer usuarioId;

    // Identificador do evento associado (imutável)
    private final Integer eventoId;

    // Indica se este organizador é o criador / dono principal do evento
    private final boolean criadorPrincipal;

    /**
     * Construtor para criação de um novo vínculo antes de ser persistido.
     *
     * @param usuarioId ID do usuário que será organizador
     * @param eventoId ID do evento
     * @param criadorPrincipal true se for o criador original do evento
     */
    public VinculoOrganizador(Integer usuarioId, Integer eventoId, boolean criadorPrincipal) {
        validarIdUsuario(usuarioId);
        validarIdEvento(eventoId);
        this.id = null;
        this.usuarioId = usuarioId;
        this.eventoId = eventoId;
        this.criadorPrincipal = criadorPrincipal;
    }

    /**
     * Construtor para reconstrução via repositório de persistência.
     *
     * @param id ID do vínculo persistido
     * @param usuarioId ID do usuário
     * @param eventoId ID do evento
     * @param criadorPrincipal true se for o criador principal
     */
    public VinculoOrganizador(Long id, Integer usuarioId, Integer eventoId, boolean criadorPrincipal) {
        validarIdUsuario(usuarioId);
        validarIdEvento(eventoId);
        this.id = id;
        this.usuarioId = usuarioId;
        this.eventoId = eventoId;
        this.criadorPrincipal = criadorPrincipal;
    }

    // Regras de validação de invariantes
    private void validarIdUsuario(Integer usuarioId) {
        if (usuarioId == null || usuarioId <= 0) {
            throw new RegraNegocioException("O ID do usuário para o vínculo de organizador deve ser válido.");
        }
    }

    private void validarIdEvento(Integer eventoId) {
        if (eventoId == null || eventoId <= 0) {
            throw new RegraNegocioException("O ID do evento para o vínculo de organizador deve ser válido.");
        }
    }

    // Getters imutáveis
    public Long getId() {
        return id;
    }

    public Integer getUsuarioId() {
        return usuarioId;
    }

    public Integer getEventoId() {
        return eventoId;
    }

    public boolean isCriadorPrincipal() {
        return criadorPrincipal;
    }
}
