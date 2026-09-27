package br.ueg.eventos.domain.model;

import br.ueg.eventos.domain.exception.RegraNegocioException;

public class Inscricao {

    private Long id;
    private Long idAtividade;
    private Long idUsuario;
    private boolean cancelada;

    public Inscricao(Long idAtividade, Long idUsuario) {
        validarId(idAtividade, "atividade");
        validarId(idUsuario, "usuario");

        this.idAtividade = idAtividade;
        this.idUsuario = idUsuario;
        this.cancelada = false;
    }

    public Inscricao(
            Long id,
            Long idAtividade,
            Long idUsuario,
            boolean cancelada
    ) {
        if (id != null) {
            validarId(id, "inscrição");
        }
        validarId(idAtividade, "atividade");
        validarId(idUsuario, "usuario");

        this.id = id;
        this.idAtividade = idAtividade;
        this.idUsuario = idUsuario;
        this.cancelada = cancelada;
    }

    public Long getId() {
        return id;
    }

    public Long getIdAtividade() {
        return idAtividade;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public boolean isCancelada() {
        return cancelada;
    }

    public void cancelar() {

        if (cancelada) {
            throw new RegraNegocioException(
                    "A inscrição já está cancelada."
            );
        }

        this.cancelada = true;
    }

    public void reativar() {

        if (!cancelada) {
            throw new RegraNegocioException(
                    "A inscrição já está ativa."
            );
        }

        this.cancelada = false;
    }

    public boolean estaAtiva() {
        return !cancelada;
    }

    private void validarId(Long id, String entidade) {

        if (id == null || id <= 0) {
            throw new RegraNegocioException(
                    "O id de " + entidade + " deve ser válido."
            );
        }
    }
}