package br.ueg.eventos.domain.model;

import br.ueg.eventos.domain.exception.RegraNegocioException;

public class Resposta {

    private Integer id;
    private Integer idQuestao;
    private Integer idUsuario;
    private String valor;

    // Construtor para criação de uma submissão de resposta.
    public Resposta(Integer id, Integer idQuestao, Integer idUsuario, String valor) {
        if (idQuestao == null || idQuestao <= 0) {
            throw new RegraNegocioException("A resposta deve estar vinculada a um ID de questão válido.");
        }
        if (idUsuario == null || idUsuario <= 0) {
            throw new RegraNegocioException("A resposta deve identificar um ID de usuário válido.");
        }
        this.id = id;
        this.idQuestao = idQuestao;
        this.idUsuario = idUsuario;
        setValorComValidacao(valor);
    }

    // Permite que o participante edite/atualize o conteúdo da sua resposta (se for permitido(RN-14)).
    public void alterarValor(String novoValor) {
        setValorComValidacao(novoValor);
    }

    private void setValorComValidacao(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new RegraNegocioException("O conteúdo da resposta não pode ficar em branco.");
        }
        this.valor = valor;
    }

    public Integer getId() {
        return id;
    }
    public Integer getIdQuestao() {
        return idQuestao;
    }
    public Integer getIdUsuario() {
        return idUsuario;
    }
    public String getValor() {
        return valor;
    }
}