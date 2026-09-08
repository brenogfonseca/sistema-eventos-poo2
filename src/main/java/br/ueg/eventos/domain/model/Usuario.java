import java.util.regex.Pattern;

public class Usuario {

    //Padrões e constantes
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");
    private static final Pattern SENHA_PATTERN = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&*]).{8,}$");

    //Atributos
    private Integer id;
    private String nome;
    private String email;
    private String senha;
    private Perfil perfil;


    //Construtores:
    //CONSTRUTOR: Para criar um usuário NOVO (O Java deixa o 'id' como null temporariamente, quem vai preencher o id é o banco de dados)
    //Usando o this() para chamar o outro construtor e evitar duplicação de código. Clean Code, pessoal!! =^.^=
    public Usuario(String nome, String email, String senha, Perfil perfil) {
        this(null, nome, email, senha, perfil);
    }


    //CONSTRUTOR: Para buscas.
    public Usuario(Integer id, String nome, String email, String senha, Perfil perfil) {
        this.id = id;
        alterarNome(nome);
        alterarEmail(email);
        alterarSenha(senha);

        //Validação simples apenas para garantir que não enviaram null
        if (perfil == null) throw new IllegalArgumentException("O perfil não pode ser nulo!");
        
        this.perfil = perfil;
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

    //MÉTODO BLINDADO: Exige saber QUEM está mandando alterar
    public void alterarPerfil(Usuario usuarioExecutor, Perfil novoPerfil) {
        if (novoPerfil == null) {
            throw new IllegalArgumentException("O perfil não pode ser nulo!");
        }
        
        //Regra de segurança corporativa direto no coração do domínio (Evita erros na camada Service)
        if (!usuarioExecutor.podeAlterarPerfil()) {
            throw new SecurityException("Operação negada: Apenas administradores podem alterar o perfil de um usuário.");
        }
        
        this.perfil = novoPerfil;
    }


    //Getters (Não possui Setters, pois se não qualquer classe pode alterar os atributos do usuário)
    public Integer getId() {return id;}
    public String getNome() {return nome;}
    public String getEmail() {return email;}
    public String getSenha() {return senha;}
    public Perfil getPerfil() {return perfil;}


    //Composição: (Permissões de cada perfil)
    //Foram adicionadas regras cuja execução depende apenas do perfil do usuário. Não foram adicionadas regras que dependem se o usuário é organizador ou participante, pois isso é função da classe que sabe quem é organizador ou participante do evento.
    public boolean podeAlterarPerfil() {
        return perfil == Perfil.ADMINISTRADOR;
    }

    public boolean podeGerenciarUsuariosEOutrosEventos() {
        return this.perfil == Perfil.ADMINISTRADOR;
    }

    public boolean podeIniciarCadastro() {
        return this.perfil == Perfil.VISITANTE;
    }



}

//OBS1: O enum Perfil é usado para definir o perfil do usuário, enquanto o enum TipoUsuario foi removido da classe Usuario, pois se permanecem na mesma classe um administrador ou um usuario não poderiam ser um participante do evento ou um organizador.
//OBS2: Não iremos usar herança para os perfis de usuário, pois isso os tornaria imutáveis durante a execução além de gerar uma complexidade desnecessária. Iremos usar composição para configurar as mermissões de cada perfil.