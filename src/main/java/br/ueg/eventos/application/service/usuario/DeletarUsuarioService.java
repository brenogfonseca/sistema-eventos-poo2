package br.ueg.eventos.application.service.usuario;

import br.ueg.eventos.application.port.in.usuario.DeletarUsuarioPort;
import br.ueg.eventos.application.port.out.UsuarioRepository;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Usuario;

/**
 * Serviço de aplicação para o caso de uso de deletar um usuário.
 * Apenas ADMINISTRADOR pode executar. Regra de segurança: um administrador
 * não pode deletar a si mesmo para evitar perda de acesso administrativo.
 */
public class DeletarUsuarioService implements DeletarUsuarioPort {

    private final UsuarioRepository usuarioRepository;

    public DeletarUsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public void executar(ComandoDeletarUsuario comando) {
        Usuario executor = carregarUsuario(comando.idExecutor,
                "Executor não encontrado com o id: " + comando.idExecutor);

        if (!executor.podeGerenciarUsuariosEOutrosEventos()) {
            throw new RegraNegocioException(
                    "Acesso negado: apenas administradores podem remover usuários.");
        }

        // Garante que o usuário alvo existe antes de tentar deletar
        carregarUsuario(comando.idAlvo,
                "Usuário não encontrado com o id: " + comando.idAlvo);

        if (executor.getId().equals(comando.idAlvo)) {
            throw new RegraNegocioException(
                    "Operação negada: um administrador não pode remover a si mesmo.");
        }

        usuarioRepository.deletar(comando.idAlvo);
    }

    private Usuario carregarUsuario(Integer id, String mensagemErro) {
        return usuarioRepository.buscarPorId(id)
                .orElseThrow(() -> new RegraNegocioException(mensagemErro));
    }
}
