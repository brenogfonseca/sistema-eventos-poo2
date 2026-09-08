import java.util.regex.Pattern;

public class Usuario {

    //Padrões e constantes
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");
    private static final Pattern SENHA_PATTERN = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&*]).{8,}$");

    //atributos
    private Integer id;
    private String nome;
    private String email;
    private String senha;
    private Perfil perfil;
    private TipoUsuario tipo;


    //construtores:
    // CONSTRUTOR: Para criar um usuário NOVO (O Java deixa o 'id' como null temporariamente, quem vai preencher é o banco de dados)
    public Usuario(String nome, String email, String senha, Perfil perfil, TipoUsuario tipo) {
        this.id = null; // O banco de dados vai gerar esse cara depois!
        alterarNome(nome);
        alterarEmail(email);
        alterarSenha(senha);
        
        // Validação simples apenas para garantir que não enviaram null
        if (perfil == null) throw new IllegalArgumentException("O perfil não pode ser nulo!");
        if (tipo == null) throw new IllegalArgumentException("O tipo não pode ser nulo!");
        
        this.perfil = perfil;
        this.tipo = tipo;
    }


    // CONSTRUTOR: Para buscas.
    public Usuario(Integer id, String nome, String email, String senha, Perfil perfil, TipoUsuario tipo) {
        this.id = id;
        alterarNome(nome);
        alterarEmail(email);
        alterarSenha(senha);

        // Validação simples apenas para garantir que não enviaram null
        if (perfil == null) throw new IllegalArgumentException("O perfil não pode ser nulo!");
        if (tipo == null) throw new IllegalArgumentException("O tipo não pode ser nulo!");

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

    public void alterarSenha(String novaSenha) {
        if (novaSenha == null || novaSenha.trim().isEmpty()) {
            throw new IllegalArgumentException("A senha não pode estar em branco!");
        }

        if (!SENHA_PATTERN.matcher(novaSenha).matches()) {
            throw new IllegalArgumentException("A senha deve conter pelo menos 8 caracteres, incluindo letras maiúsculas, minúsculas, números e caracteres especiais.");
        }

        this.senha = novaSenha;
    }


    //Getters (Não possui Setters, pois se não qualquer classe pode alterar os atributos do usuário)
    public Integer getId() {return id;}
    public String getNome() {return nome;}
    public String getEmail() {return email;}
    public String getSenha() {return senha;}
    public Perfil getPerfil() {return perfil;}
    public TipoUsuario getTipo() {return tipo;}

}