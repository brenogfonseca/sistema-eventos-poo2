package br.ueg.eventos.application.service.usuario;

import br.ueg.eventos.application.port.in.usuario.BuscarUsuarioPorIdPort;
import br.ueg.eventos.application.port.out.UsuarioRepository;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Usuario;

/**
 * Serviço de aplicação para o caso de uso de buscar um usuário pelo id.
 * ADMINISTRADOR acessa qualquer usuário; USUARIO acessa apenas o próprio perfil.
 */
public class BuscarUsuarioPorIdService implements BuscarUsuarioPorIdPort {

    private final UsuarioRepository usuarioRepository;

    public BuscarUsuarioPorIdService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public Usuario executar(ComandoBuscarUsuarioPorId comando) {
        Usuario executor = carregarUsuario(comando.idExecutor,
                "Executor não encontrado com o id: " + comando.idExecutor);

        Usuario alvo = carregarUsuario(comando.idAlvo,
                "Usuário não encontrado com o id: " + comando.idAlvo);

        boolean ehProprioUsuario = executor.getId().equals(alvo.getId());
        boolean ehAdministrador = executor.podeGerenciarUsuariosEOutrosEventos();

        if (!ehProprioUsuario && !ehAdministrador) {
            throw new RegraNegocioException(
                    "Acesso negado: você só pode consultar seus próprios dados.");
        }

        return alvo;
    }

    private Usuario carregarUsuario(Integer id, String mensagemErro) {
        return usuarioRepository.buscarPorId(id)
                .orElseThrow(() -> new RegraNegocioException(mensagemErro));
    }
}
