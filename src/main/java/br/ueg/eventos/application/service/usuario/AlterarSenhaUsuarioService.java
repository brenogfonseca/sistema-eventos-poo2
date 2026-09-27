package br.ueg.eventos.application.service.usuario;

import br.ueg.eventos.application.port.in.usuario.AlterarSenhaUsuarioPort;
import br.ueg.eventos.application.port.out.PasswordEncryptor;
import br.ueg.eventos.application.port.out.UsuarioRepositoryPort;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Usuario;

/**
 * Serviço de aplicação responsável pelo caso de uso de alterar a senha de um usuário.
 * Localiza o usuário, criptografa a nova senha e delega a validação e a
 * atualização do hash ao próprio domínio (Usuario.alterarSenha).
 */
public class AlterarSenhaUsuarioService implements AlterarSenhaUsuarioPort {

    private final UsuarioRepositoryPort usuarioRepository;
    private final PasswordEncryptor passwordEncryptor;

    public AlterarSenhaUsuarioService(UsuarioRepositoryPort usuarioRepository, PasswordEncryptor passwordEncryptor) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncryptor = passwordEncryptor;
    }

    @Override
    public Usuario executar(ComandoAlterarSenha comando) {
        Usuario usuario = usuarioRepository.buscarPorId(comando.idUsuario)
                .orElseThrow(() -> new RegraNegocioException(
                        "Usuário não encontrado com o id: " + comando.idUsuario));

        String novoHash = passwordEncryptor.criptografar(comando.novaSenhaLimpa);

        // O domínio valida a força da nova senha e atualiza o hash internamente
        usuario.alterarSenha(comando.novaSenhaLimpa, novoHash);

        return usuarioRepository.salvar(usuario);
    }
}
