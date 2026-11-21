package br.ueg.eventos.domain.model;

import br.ueg.eventos.domain.exception.RegraNegocioException;
import java.util.ArrayList;
import java.util.List;

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
    private List<String> opcoes = List.of();
    private Integer escalaMinima;
    private Integer escalaMaxima;

   // Construtor para CRIAR uma nova questão (sem ID)
    public Questao(String enunciado, TipoQuestao tipo, Integer idQuestionario) {
        if (idQuestionario == null || idQuestionario <= 0) {
            throw new RegraNegocioException("A questão precisa estar vinculada a um ID de questionário válido.");
        }
        this.idQuestionario = idQuestionario;
        setEnunciadoComValidacao(enunciado);
        setTipoComValidacao(tipo);
    }

    // Construtor para RECUPERAR do banco (com ID)
    public Questao(Integer id, String enunciado, TipoQuestao tipo, Integer idQuestionario) {
        this(enunciado, tipo, idQuestionario); // Reaproveita as validações
        this.id = id;
    }

    public Questao(String enunciado, TipoQuestao tipo, Integer idQuestionario,
                   List<String> opcoes, Integer escalaMinima, Integer escalaMaxima) {
        this(enunciado, tipo, idQuestionario);
        configurarFormato(opcoes, escalaMinima, escalaMaxima);
    }

    public Questao(Integer id, String enunciado, TipoQuestao tipo, Integer idQuestionario,
                   List<String> opcoes, Integer escalaMinima, Integer escalaMaxima) {
        this(enunciado, tipo, idQuestionario, opcoes, escalaMinima, escalaMaxima);
        if (id == null || id <= 0) throw new RegraNegocioException("O id da questão deve ser válido.");
        this.id = id;
    }

    // Permite a alteração/atualização do enunciado da questão.
    public void alterarEnunciado(String novoEnunciado) {
        setEnunciadoComValidacao(novoEnunciado);
    }

    // Permite a alteração/atualização do tipo da questão.
    public void alterarTipo(TipoQuestao novoTipo) {
        setTipoComValidacao(novoTipo);
    }

    public void configurarFormato(List<String> novasOpcoes, Integer minimo, Integer maximo) {
        if (tipo == TipoQuestao.ESCOLHA_UNICA) {
            List<String> limpas = novasOpcoes == null ? List.of() : novasOpcoes.stream()
                    .filter(opcao -> opcao != null && !opcao.isBlank())
                    .map(String::trim).distinct().toList();
            if (limpas.size() < 2 || limpas.stream().anyMatch(opcao -> opcao.contains("\n") || opcao.contains("\r"))) {
                throw new RegraNegocioException("Questões de escolha única precisam de pelo menos duas opções distintas, uma por linha.");
            }
            this.opcoes = List.copyOf(limpas);
            this.escalaMinima = null;
            this.escalaMaxima = null;
        } else if (tipo == TipoQuestao.ESCALA_NUMERICA) {
            if (minimo == null || maximo == null || minimo >= maximo) {
                throw new RegraNegocioException("Informe uma escala numérica válida com mínimo menor que máximo.");
            }
            this.opcoes = List.of();
            this.escalaMinima = minimo;
            this.escalaMaxima = maximo;
        } else {
            if ((novasOpcoes != null && !novasOpcoes.isEmpty()) || minimo != null || maximo != null) {
                throw new RegraNegocioException("Questões textuais não aceitam opções ou faixa numérica.");
            }
            this.opcoes = List.of();
            this.escalaMinima = null;
            this.escalaMaxima = null;
        }
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
    public List<String> getOpcoes() { return new ArrayList<>(opcoes); }
    public Integer getEscalaMinima() { return escalaMinima; }
    public Integer getEscalaMaxima() { return escalaMaxima; }
}
