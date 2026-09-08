import java.util.regex.Pattern;

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
        alterarNome(nome);
        alterarEmail(email);
        this.senha = senha;
        this.perfil = perfil;
        this.tipo = tipo;
    }

    //Métodos de ação
    public void alterarNome(String novoNome) {
        if (novoNome == null || novoNome.trim().isEmpty()) {
            throw new IllegalArgumentException("O nome não pode estar em branco!");
        }
        this.nome = novoNome;
    }

    public void alterarEmail(String novoEmail) {
        if (novoEmail == null || novoEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("O email não pode estar em branco!");
        }

        if (!EMAIL_PATTERN.matcher(novoEmail).matches()) {
            throw new IllegalArgumentException("O formato do e-mail é inválido.");
        }

        this.email = novoEmail;
    }

    //Validações


    //Getters (Não possui Setters, pois se não qualquer classe pode alterar os atributos do usuário)
    public Integer getId() {return id;}
    public String getNome() {return nome;}
    public String getEmail() {return email;}
    public String getSenha() {return senha;}
    public String getPerfil() {return perfil;}
    public String getTipo() {return tipo;}

}