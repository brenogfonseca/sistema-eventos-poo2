package br.ueg.eventos.domain.model;

public class Trilha {

    private Long id;
    private String nome;

    public Trilha(String nome) {
        this.nome = nome;
    }

    public Trilha(Long id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}
