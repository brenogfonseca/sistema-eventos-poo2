package br.ueg.eventos.domain.model;

import br.ueg.eventos.domain.exception.RegraNegocioException;

public class PessoaProgramacao {
    private Long id;
    private final String nome;
    private final String organizacao;
    private final String biografia;
    private final String fotoUrl;

    public PessoaProgramacao(String nome, String organizacao, String biografia, String fotoUrl) {
        if (nome == null || nome.isBlank()) throw new RegraNegocioException("O nome da pessoa é obrigatório.");
        this.nome = nome.trim();
        this.organizacao = vazioComoNulo(organizacao);
        this.biografia = vazioComoNulo(biografia);
        this.fotoUrl = vazioComoNulo(fotoUrl);
    }

    public PessoaProgramacao(Long id, String nome, String organizacao, String biografia, String fotoUrl) {
        this(nome, organizacao, biografia, fotoUrl);
        this.id = id;
    }

    private static String vazioComoNulo(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getOrganizacao() { return organizacao; }
    public String getBiografia() { return biografia; }
    public String getFotoUrl() { return fotoUrl; }
}
