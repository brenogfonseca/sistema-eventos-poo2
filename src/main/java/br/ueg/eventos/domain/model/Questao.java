package br.ueg.eventos.domain.model;

import br.ueg.eventos.domain.exception.RegraNegocioException;

public class Questao {

    // Enumeração que define os tipos de respostas permitidos pelo sistema.
    public enum TipoQuestao {
        TEXTUAL,
        ESCOLHA_UNICA,
        ESCALA_NUMERICA
    }
 
    private Integer id;
    private String enunciado;
    private TipoQuestao tipo;
    private Integer idQuestionario;

    // Construtor para criação ou reconstituição de uma Questão.
    public Questao(Integer id, String enunciado, TipoQuestao tipo, Integer idQuestionario) {
        if (idQuestionario == null || idQuestionario <= 0) {
            throw new RegraNegocioException("A questão precisa estar vinculada a um ID de questionário válido.");
        }
        this.id = id;
        this.idQuestionario = idQuestionario;
        setEnunciadoComValidacao(enunciado);
        setTipoComValidacao(tipo);
    }

    // Permite a alteração/atualização do enunciado da questão.
    public void alterarEnunciado(String novoEnunciado) {
        setEnunciadoComValidacao(novoEnunciado);
    }

    // Permite a alteração/atualização do tipo da questão.
    public void alterarTipo(TipoQuestao novoTipo) {
        setTipoComValidacao(novoTipo);
    }

    private void setEnunciadoComValidacao(String enunciado) {
        if (enunciado == null || enunciado.trim().isEmpty()) {
            throw new RegraNegocioException("O enunciado da questão é obrigatório e não pode ser vazio.");
        }
        this.enunciado = enunciado;
    }

    private void setTipoComValidacao(TipoQuestao tipo) {
        if (tipo == null) {
            throw new RegraNegocioException("O tipo da questão (TEXTUAL, ESCOLHA_UNICA, ESCALA_NUMERICA) deve ser informado.");
        }
        this.tipo = tipo;
    }

    public Integer getId() {
        return id;
    }
    public String getEnunciado() {
        return enunciado;
    }
    public TipoQuestao getTipo() {
        return tipo;
    }
    public Integer getIdQuestionario() {
        return idQuestionario;
    }
}