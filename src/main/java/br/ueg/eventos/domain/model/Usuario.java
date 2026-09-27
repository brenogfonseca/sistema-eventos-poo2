package br.ueg.eventos.domain.model;

import br.ueg.eventos.domain.exception.RegraNegocioException;
import java.util.regex.Pattern;

public class Usuario {

    // Padrões e constantes
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");
    private static final Pattern SENHA_PATTERN = Pattern
            .compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&*]).{8,}$");

    // Atributos
    private Integer id;
    private String nome;
    private String email;
    private String senhaHash; // A senha será armazenada como hash para maior segurança
    private Perfil perfil;

    // Construtores:
    // CONSTRUTOR 1: Para criar um usuário NOVO (O Java deixa o 'id' como null
    // temporariamente, quem vai preencher o id é o banco de dados)
    public Usuario(String nome, String email, String senhaLimpa, String hashCriptografado, Perfil perfil) {
        this.id = null;
        alterarNome(nome);
        alterarEmail(email);
        // Valida se a senha digitada na tela é forte antes de aceitar o hash
        validarFormatoSenha(senhaLimpa);

        if (hashCriptografado == null || hashCriptografado.trim().isEmpty()) {
            throw new RegraNegocioException("O hash da senha é obrigatório!");
        }
        this.senhaHash = hashCriptografado;

        if (perfil == null)
            throw new RegraNegocioException("O perfil não pode ser nulo!");
        this.perfil = perfil;
    }

    // CONSTRUTOR 2: Para buscas.
    public Usuario(Integer id, String nome, String email, String hashCriptografado, Perfil perfil) {
        this.id = id;
        alterarNome(nome);
        alterarEmail(email);
        // Apenas atribui o hash do banco sem rodar a validação de padrão forte (que
        // quebraria o carregamento)
        if (hashCriptografado == null || hashCriptografado.trim().isEmpty()) {
            throw new RegraNegocioException("O hash da senha é obrigatório!");
        }
        this.senhaHash = hashCriptografado;

        if (perfil == null)
            throw new RegraNegocioException("O perfil não pode ser nulo!");
        this.perfil = perfil;
    }

    // Métodos de ação
    public void alterarNome(String novoNome) {
        if (novoNome == null || novoNome.trim().isEmpty()) {
            throw new RegraNegocioException("O nome não pode estar em branco!");
        }
        this.nome = novoNome;
    }

    public void alterarEmail(String novoEmail) {
        if (novoEmail == null || novoEmail.trim().isEmpty()) {
            throw new RegraNegocioException("O email não pode estar em branco!");
        }

        if (!EMAIL_PATTERN.matcher(novoEmail).matches()) {
            throw new RegraNegocioException("O formato do e-mail é inválido.");
        }

        this.email = novoEmail;
    }

    // Método auxiliar focado puramente em checar a força da senha digitada
    public void validarFormatoSenha(String senhaLimpa) {
        if (senhaLimpa == null || senhaLimpa.trim().isEmpty()) {
            throw new RegraNegocioException("A senha não pode estar em branco!");
        }

        if (!SENHA_PATTERN.matcher(senhaLimpa).matches()) {
            throw new RegraNegocioException(
                    "A senha deve conter pelo menos 8 caracteres, incluindo letras maiúsculas, minúsculas, números e caracteres especiais.");
        }
    }

    // Método de ação para quando um usuário autenticado quiser trocar sua senha
    // antiga por uma nova
    public void alterarSenha(String novaSenhaLimpa, String novoHashCriptografado) {
        validarFormatoSenha(novaSenhaLimpa);

        if (novoHashCriptografado == null || novoHashCriptografado.trim().isEmpty()) {
            throw new RegraNegocioException("O novo hash da senha é obrigatório!");
        }
        this.senhaHash = novoHashCriptografado;
    }

    // MÉTODO BLINDADO: Exige saber QUEM está mandando alterar
    public void alterarPerfil(Usuario usuarioExecutor, Perfil novoPerfil) {
        if (novoPerfil == null) {
            throw new RegraNegocioException("O perfil não pode ser nulo!");
        }

        // Regra de segurança corporativa direto no coração do domínio (Evita erros na
        // camada Service)
        if (!usuarioExecutor.podeAlterarPerfil()) {
            throw new RegraNegocioException(
                    "Operação negada: Apenas administradores podem alterar o perfil de um usuário.");
        }

        this.perfil = novoPerfil;
    }

    // Getters (Não possui Setters, pois se não qualquer classe pode alterar os
    // atributos do usuário)
    public Integer getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public Perfil getPerfil() {
        return perfil;
    }

    // Composição: (Permissões de cada perfil)
    // Foram adicionadas regras cuja execução depende apenas do perfil do usuário.
    // Não foram adicionadas regras que dependem se o usuário é organizador ou
    // participante, pois isso é função da classe que sabe quem é organizador ou
    // participante do evento.
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

// OBS1: O enum Perfil é usado para definir o perfil do usuário, enquanto o enum
// TipoUsuario foi removido da classe Usuario, pois se permanecem na mesma
// classe um administrador ou um usuario não poderiam ser um participante do
// evento ou um organizador.
// OBS2: Não iremos usar herança para os perfis de usuário, pois isso os
// tornaria imutáveis durante a execução além de gerar uma complexidade
// desnecessária. Iremos usar composição para configurar as mermissões de cada
// perfil.