package br.ueg.eventos.application.service.usuario;

import br.ueg.eventos.application.port.in.usuario.AutenticarUsuarioPort;
import br.ueg.eventos.application.port.out.AuthenticationTokenPort;
import br.ueg.eventos.application.port.out.PasswordEncryptor;
import br.ueg.eventos.application.port.out.UsuarioRepositoryPort;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Usuario;

import java.util.Locale;

public final class AutenticarUsuarioService implements AutenticarUsuarioPort {
    private final UsuarioRepositoryPort usuarios;
    private final PasswordEncryptor senhas;
    private final AuthenticationTokenPort tokens;

    public AutenticarUsuarioService(UsuarioRepositoryPort usuarios,
                                    PasswordEncryptor senhas,
                                    AuthenticationTokenPort tokens) {
        this.usuarios = usuarios;
        this.senhas = senhas;
        this.tokens = tokens;
    }

    @Override
    public DadosAutenticacao executar(String email, String senha) {
        if (email == null || email.isBlank() || senha == null || senha.isEmpty()) {
            throw new RegraNegocioException("Informe e-mail e senha.");
        }
        Usuario usuario = usuarios.buscarPorEmail(email.trim().toLowerCase(Locale.ROOT))
                .filter(u -> senhas.verificar(senha, u.getSenhaHash()))
                .orElseThrow(() -> new RegraNegocioException("E-mail ou senha incorretos."));

        if (senhas.precisaAtualizar(usuario.getSenhaHash())) {
            usuario.alterarSenha(senha, senhas.criptografar(senha));
            usuario = usuarios.salvar(usuario);
        }

        AuthenticationTokenPort.Token token = tokens.emitir(usuario.getId(), usuario.getPerfil().name());
        return new DadosAutenticacao(usuario.getId(), usuario.getNome(), usuario.getEmail(),
                usuario.getPerfil().name(), token.valor(), token.expiraEm());
    }
}
