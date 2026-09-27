package br.ueg.eventos.application.service.usuario;

import br.ueg.eventos.application.port.in.usuario.CadastrarUsuarioPort;
import br.ueg.eventos.application.port.out.PasswordEncryptor;
import br.ueg.eventos.application.port.out.UsuarioRepositoryPort;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Usuario;

/**
 * Serviço de aplicação responsável pelo caso de uso de cadastrar um novo usuário.
 * Orquestra a validação de e-mail duplicado, a criptografia da senha e a
 * persistência, delegando as regras de negócio ao próprio domínio (Usuario).
 */
public class CadastrarUsuarioService implements CadastrarUsuarioPort {

    private final UsuarioRepositoryPort usuarioRepository;
    private final PasswordEncryptor passwordEncryptor;

    public CadastrarUsuarioService(UsuarioRepositoryPort usuarioRepository, PasswordEncryptor passwordEncryptor) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncryptor = passwordEncryptor;
    }

    @Override
    public Usuario executar(ComandoCadastrarUsuario comando) {
        validarEmailDisponivel(comando.email);

        String hashCriptografado = passwordEncryptor.criptografar(comando.senhaLimpa);

        Usuario novoUsuario = new Usuario(
                comando.nome,
                comando.email,
                comando.senhaLimpa,
                hashCriptografado,
                comando.perfil
        );

        return usuarioRepository.salvar(novoUsuario);
    }

    private void validarEmailDisponivel(String email) {
        usuarioRepository.buscarPorEmail(email).ifPresent(u -> {
            throw new RegraNegocioException("Já existe um usuário cadastrado com o e-mail: " + email);
        });
    }
}
