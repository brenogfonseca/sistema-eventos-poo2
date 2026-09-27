package br.ueg.eventos.application.service.usuario;

import br.ueg.eventos.application.port.in.usuario.BuscarUsuarioPorEmailPort;
import br.ueg.eventos.application.port.out.UsuarioRepository;
import br.ueg.eventos.domain.model.Usuario;

import java.util.Optional;

/**
 * Serviço de aplicação responsável pelo caso de uso de buscar um usuário pelo e-mail.
 * Delega diretamente ao repositório, sem lógica de negócio adicional neste caso.
 */
public class BuscarUsuarioPorEmailService implements BuscarUsuarioPorEmailPort {

    private final UsuarioRepository usuarioRepository;

    public BuscarUsuarioPorEmailService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public Optional<Usuario> executar(String email) {
        return usuarioRepository.buscarPorEmail(email);
    }
}
