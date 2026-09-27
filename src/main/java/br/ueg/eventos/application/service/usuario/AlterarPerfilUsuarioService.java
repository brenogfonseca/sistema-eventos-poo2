package br.ueg.eventos.application.service.usuario;

import br.ueg.eventos.application.port.in.usuario.AlterarPerfilUsuarioPort;
import br.ueg.eventos.application.port.out.UsuarioRepositoryPort;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Usuario;

/**
 * Serviço de aplicação para o caso de uso de alterar o perfil de um usuário.
 * A validação de permissão é delegada diretamente ao domínio:
 * {@code Usuario.alterarPerfil(executor, novoPerfil)} lança
 * {@link RegraNegocioException} se o executor não for ADMINISTRADOR.
 */
public class AlterarPerfilUsuarioService implements AlterarPerfilUsuarioPort {

    private final UsuarioRepositoryPort usuarioRepository;

    public AlterarPerfilUsuarioService(UsuarioRepositoryPort usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public Usuario executar(ComandoAlterarPerfil comando) {
        Usuario executor = carregarUsuario(comando.idExecutor,
                "Executor não encontrado com o id: " + comando.idExecutor);

        Usuario alvo = carregarUsuario(comando.idAlvo,
                "Usuário não encontrado com o id: " + comando.idAlvo);

        // A regra de negócio (somente ADMINISTRADOR pode alterar perfil)
        // está encapsulada no próprio domínio — não repetimos aqui.
        alvo.alterarPerfil(executor, comando.novoPerfil);

        return usuarioRepository.salvar(alvo);
    }

    private Usuario carregarUsuario(Integer id, String mensagemErro) {
        return usuarioRepository.buscarPorId(id)
                .orElseThrow(() -> new RegraNegocioException(mensagemErro));
    }
}
