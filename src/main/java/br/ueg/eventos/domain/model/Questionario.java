package br.ueg.eventos.domain.model;

import br.ueg.eventos.domain.exception.RegraNegocioException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Questionario {

    private Integer id;
    private Integer idEvento;
    private String titulo;
    private List<Questao> questoes;

    // Construtor para inicialização ou reconstituição do Questionário.
    public Questionario(Integer id, Integer idEvento, String titulo) {
        if (idEvento == null || idEvento <= 0) {
            throw new RegraNegocioException("O questionário deve obrigatoriamente estar vinculado a um ID de evento válido.");
        }
        setTituloComValidacao(titulo);
        
        this.id = id;
        this.idEvento = idEvento;
        this.questoes = new ArrayList<>();
    }

    // Permite reconfigurar/atualizar o título do questionário.
    public void alterarTitulo(String novoTitulo) {
        setTituloComValidacao(novoTitulo);
    }

    // Centraliza a validação das invariantes do título.
    private void setTituloComValidacao(String titulo) {
        if (titulo == null || titulo.trim().isEmpty()) {
            throw new RegraNegocioException("O título do questionário não pode ser vazio.");
        }
        this.titulo = titulo;
    }

    // Adiciona uma nova questão à coleção do questionário, impedindo duplicatas.
    public void adicionarQuestao(Questao questao) {
        if (questao == null) {
            throw new RegraNegocioException("Não é possível adicionar uma questão nula ao questionário.");
        }
        if (this.questoes.contains(questao)) {
            throw new RegraNegocioException("Esta questão já foi adicionada ao questionário.");
        }
        this.questoes.add(questao);
    }

    // Remove uma questão existente da coleção do questionário.
    public void removerQuestao(Questao questao) {
        if (questao == null || !this.questoes.contains(questao)) {
            throw new RegraNegocioException("A questão informada não existe neste questionário.");
        }
        this.questoes.remove(questao);
    }

    // Retorna uma visão não modificável da lista de questões para preservar o encapsulamento.
    public List<Questao> getQuestoes() {
        return Collections.unmodifiableList(questoes);
    }

    public Integer getId() {
        return id;
    }
    public Integer getIdEvento() {
        return idEvento;
    }
    public String getTitulo() {
        return titulo;
    }
}