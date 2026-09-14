package br.ueg.eventos.domain.model;

public class Local {

    private Long id;
    private String nome;

    public Local(String nome) {
        this.nome = nome;
    }

    public Local(Long id, String nome) {
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
