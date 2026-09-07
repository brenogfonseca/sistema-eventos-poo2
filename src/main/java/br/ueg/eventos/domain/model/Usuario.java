public class Usuario {

    //atributos
    private Integer id;
    private String nome;
    private String email;
    private String senha;
    private String perfil;
    private String tipo;


    //construtores
    public Usuario(Integer id, String nome, String email, String senha, String perfil, String tipo) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.perfil = perfil;
        this.tipo = tipo;
    }

    