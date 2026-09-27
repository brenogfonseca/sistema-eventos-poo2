package br.ueg.eventos.domain.model;

import br.ueg.eventos.domain.exception.RegraNegocioException;

public class Inscricao {

    private Integer id;
    private Integer idAtividade;
    private Integer idUsuario;
    private boolean cancelada;

    public Inscricao(Integer idAtividade, Integer idUsuario) {
        validarId(idAtividade, "atividade");
        validarId(idUsuario, "usuario");

        this.idAtividade = idAtividade;
        this.idUsuario = idUsuario;
        this.cancelada = false;
    }

    public Inscricao(
            Integer id,
            Integer idAtividade,
            Integer idUsuario,
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

    public Integer getId() {
        return id;
    }

    public Integer getIdAtividade() {
        return idAtividade;
    }

    public Integer getIdUsuario() {
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

    private void validarId(Integer id, String entidade) {

        if (id == null || id <= 0) {
            throw new RegraNegocioException(
                    "O id de " + entidade + " deve ser válido."
            );
        }
    }
}