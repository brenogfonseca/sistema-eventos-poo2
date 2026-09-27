package br.ueg.eventos.application.service.usuario;

import br.ueg.eventos.application.port.in.usuario.AlterarDadosUsuarioPort;
import br.ueg.eventos.application.port.out.UsuarioRepository;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Usuario;

/**
 * Serviço de aplicação para o caso de uso de alterar dados básicos do usuário.
 * O próprio usuário pode alterar seus dados; o ADMINISTRADOR pode alterar
 * os dados de qualquer usuário.
 */
public class AlterarDadosUsuarioService implements AlterarDadosUsuarioPort {

    private final UsuarioRepository usuarioRepository;

    public AlterarDadosUsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public Usuario executar(ComandoAlterarDados comando) {
        Usuario executor = carregarUsuario(comando.idExecutor,
                "Executor não encontrado com o id: " + comando.idExecutor);

        Usuario alvo = carregarUsuario(comando.idAlvo,
                "Usuário não encontrado com o id: " + comando.idAlvo);

        boolean ehProprioUsuario = executor.getId().equals(alvo.getId());
        boolean ehAdministrador = executor.podeGerenciarUsuariosEOutrosEventos();

        if (!ehProprioUsuario && !ehAdministrador) {
            throw new RegraNegocioException(
                    "Acesso negado: você só pode alterar seus próprios dados.");
        }

        // Atualização parcial: só altera o campo se um novo valor válido for enviado
        if (comando.novoNome != null && !comando.novoNome.isBlank()) {
            alvo.alterarNome(comando.novoNome);
        }
        if (comando.novoEmail != null && !comando.novoEmail.isBlank()) {
            validarEmailDisponivel(comando.novoEmail, alvo.getId());
            alvo.alterarEmail(comando.novoEmail);
        }

        return usuarioRepository.salvar(alvo);
    }

    private void validarEmailDisponivel(String email, Integer idAlvo) {
        usuarioRepository.buscarPorEmail(email).ifPresent(u -> {
            // Permite manter o mesmo e-mail (o usuário pode enviar o e-mail atual sem mudança)
            if (!u.getId().equals(idAlvo)) {
                throw new RegraNegocioException("Já existe um usuário cadastrado com o e-mail: " + email);
            }
        });
    }

    private Usuario carregarUsuario(Integer id, String mensagemErro) {
        return usuarioRepository.buscarPorId(id)
                .orElseThrow(() -> new RegraNegocioException(mensagemErro));
    }
}
