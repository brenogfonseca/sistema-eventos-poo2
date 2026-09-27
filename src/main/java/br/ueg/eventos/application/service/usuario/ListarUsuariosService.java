package br.ueg.eventos.application.service.usuario;

import br.ueg.eventos.application.port.in.usuario.ListarUsuariosPort;
import br.ueg.eventos.application.port.out.UsuarioRepositoryPort;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Usuario;

import java.util.List;

/**
 * Serviço de aplicação para o caso de uso de listar todos os usuários.
 * Apenas ADMINISTRADOR tem permissão para executar esta ação.
 */
public class ListarUsuariosService implements ListarUsuariosPort {

    private final UsuarioRepositoryPort usuarioRepository;

    public ListarUsuariosService(UsuarioRepositoryPort usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public List<Usuario> executar(ComandoListarUsuarios comando) {
        Usuario executor = carregarExecutor(comando.idExecutor);

        if (!executor.podeGerenciarUsuariosEOutrosEventos()) {
            throw new RegraNegocioException(
                    "Acesso negado: apenas administradores podem listar todos os usuários.");
        }

        return usuarioRepository.listarTodos();
    }

    private Usuario carregarExecutor(Integer idExecutor) {
        return usuarioRepository.buscarPorId(idExecutor)
                .orElseThrow(() -> new RegraNegocioException(
                        "Executor não encontrado com o id: " + idExecutor));
    }
}
