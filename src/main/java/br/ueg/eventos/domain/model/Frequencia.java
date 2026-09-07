package br.ueg.eventos.domain.model;

import br.ueg.eventos.domain.exception.RegraNegocioException;

import java.time.LocalDateTime;

public class Frequencia {

    private Integer id;
    private LocalDateTime dataHora;
    private String origem;
    private Integer idResponsavel;
    private boolean presente;
    private Integer idInscricao;
    private String tipo;

    public Frequencia(
            LocalDateTime dataHora,
            String origem,
            Integer idResponsavel,
            boolean presente,
            Integer idInscricao,
            String tipo
    ) {
        validarDados(
                dataHora,
                origem,
                idResponsavel,
                idInscricao,
                tipo
        );

        this.dataHora = dataHora;
        this.origem = origem;
        this.idResponsavel = idResponsavel;
        this.presente = presente;
        this.idInscricao = idInscricao;
        this.tipo = tipo;
    }

    public Frequencia(
            Integer id,
            LocalDateTime dataHora,
            String origem,
            Integer idResponsavel,
            boolean presente,
            Integer idInscricao,
            String tipo
    ) {
        validarId(id, "frequência");

        validarDados(
                dataHora,
                origem,
                idResponsavel,
                idInscricao,
                tipo
        );

        this.id = id;
        this.dataHora = dataHora;
        this.origem = origem;
        this.idResponsavel = idResponsavel;
        this.presente = presente;
        this.idInscricao = idInscricao;
        this.tipo = tipo;
    }

    public Integer getId() {
        return id;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public String getOrigem() {
        return origem;
    }

    public Integer getIdResponsavel() {
        return idResponsavel;
    }

    public boolean isPresente() {
        return presente;
    }

    public Integer getIdInscricao() {
        return idInscricao;
    }

    public String getTipo() {
        return tipo;
    }

    public void registrarPresenca() {
        this.presente = true;
    }

    public void registrarAusencia() {
        this.presente = false;
    }

    private void validarDados(
            LocalDateTime dataHora,
            String origem,
            Integer idResponsavel,
            Integer idInscricao,
            String tipo
    ) {

        if (dataHora == null) {
            throw new RegraNegocioException(
                    "A data e hora da frequência são obrigatórias."
            );
        }

        if (origem == null || origem.isBlank()) {
            throw new RegraNegocioException(
                    "A origem da frequência é obrigatória."
            );
        }

        if (idInscricao == null || idInscricao <= 0) {
            throw new RegraNegocioException(
                    "A inscrição deve ser válida."
            );
        }

        if (tipo == null || tipo.isBlank()) {
            throw new RegraNegocioException(
                    "O tipo da frequência é obrigatório."
            );
        }

        /*
         * O responsável pode ser obrigatório dependendo
         * da origem da marcação.
         *
         * Exemplo: lançamento manual.
         */
        if ("MANUAL".equalsIgnoreCase(origem)
                && (idResponsavel == null || idResponsavel <= 0)) {

            throw new RegraNegocioException(
                    "Uma frequência manual deve possuir um responsável."
            );
        }
    }

    private void validarId(Integer id, String entidade) {

        if (id == null || id <= 0) {
            throw new RegraNegocioException(
                    "O id de " + entidade + " deve ser válido."
            );
        }
    }
}