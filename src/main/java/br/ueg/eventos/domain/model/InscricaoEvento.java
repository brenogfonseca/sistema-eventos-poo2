package br.ueg.eventos.domain.model;

import br.ueg.eventos.domain.exception.RegraNegocioException;

/** Vínculo único de um participante com um evento; o QR do evento identifica este registro. */
public class InscricaoEvento {

    private final Long id;
    private final Long idEvento;
    private final Long idUsuario;
    private boolean cancelada;

    public InscricaoEvento(Long idEvento, Long idUsuario) {
        this(null, idEvento, idUsuario, false);
    }

    public InscricaoEvento(Long id, Long idEvento, Long idUsuario, boolean cancelada) {
        validarId(idEvento, "evento");
        validarId(idUsuario, "usuário");
        if (id != null) validarId(id, "inscrição no evento");
        this.id = id;
        this.idEvento = idEvento;
        this.idUsuario = idUsuario;
        this.cancelada = cancelada;
    }

    public Long getId() { return id; }
    public Long getIdEvento() { return idEvento; }
    public Long getIdUsuario() { return idUsuario; }
    public boolean isCancelada() { return cancelada; }
    public boolean estaAtiva() { return !cancelada; }

    public void cancelar() {
        if (cancelada) throw new RegraNegocioException("A inscrição no evento já está cancelada.");
        cancelada = true;
    }

    public void reativar() {
        if (!cancelada) throw new RegraNegocioException("A inscrição no evento já está ativa.");
        cancelada = false;
    }

    private void validarId(Long id, String entidade) {
        if (id == null || id <= 0) throw new RegraNegocioException("O id de " + entidade + " deve ser válido.");
    }
}
