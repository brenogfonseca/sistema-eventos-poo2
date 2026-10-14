package br.ueg.eventos.domain.model;

/**
 * Representa um tipo cadastrado no sistema.
 */
public class Tipo {

    // Identificador do tipo.
    private Long id;

    // Nome do tipo.
    private String nome;

    // Cria um tipo sem ID.
    public Tipo(String nome) {
        this.nome = nome;
    }

    // Cria um tipo com ID.
    public Tipo(Long id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    // Retorna o ID.
    public Long getId() {
        return id;
    }

    // Retorna o nome.
    public String getNome() {
        return nome;
    }

    // Altera o nome.
    public void setNome(String nome) {
        this.nome = nome;
    }
}