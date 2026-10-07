package br.ueg.eventos.infrastructure.adapter.out.persistence;

import br.ueg.eventos.application.port.out.UsuarioRepositoryPort;
import br.ueg.eventos.domain.model.Usuario;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Adaptador de saída: implementação in-memory do UsuarioRepository.
 * Simula a persistência em banco de dados usando um Map thread-safe.
 * Espelha o padrão já adotado pelo InMemoryEventoRepository.
 */
public class InMemoryUsuarioRepository implements UsuarioRepositoryPort {

    private final Map<Integer, Usuario> usuarios = new ConcurrentHashMap<>();
    private final AtomicInteger idGenerator = new AtomicInteger(1);

    // =========================================================================
    // USUÁRIO ADMINISTRADOR PARA TESTES
    // NOTA: Este bloco foi inserido apenas para testes locais de desenvolvimento.
    // Futuramente este bloco deve ser removido quando houver banco de dados persistente.
    // =========================================================================
    public InMemoryUsuarioRepository() {
        Integer adminId = idGenerator.getAndIncrement();
        // Senha inicial de teste: "Admin@123" -> Hash SHA-256
        String hashAdmin = new Sha256PasswordEncryptor().criptografar("Admin@123");
        Usuario admin = new Usuario(
                adminId,
                "Administrador",
                "admin@evento.com",
                hashAdmin,
                br.ueg.eventos.domain.model.Perfil.ADMINISTRADOR
        );
        usuarios.put(adminId, admin);
    }
    // =========================================================================

    @Override
    public Usuario salvar(Usuario usuario) {
        if (usuario.getId() == null) {
            Integer novoId = idGenerator.getAndIncrement();

            // Reconstrói o usuário com o id gerado (construtor de busca, sem revalidar senha)
            Usuario usuarioSalvo = new Usuario(
                    novoId,
                    usuario.getNome(),
                    usuario.getEmail(),
                    usuario.getSenhaHash(),
                    usuario.getPerfil()
            );

            usuarios.put(novoId, usuarioSalvo);
            return usuarioSalvo;
        }

        usuarios.put(usuario.getId(), usuario);
        return usuario;
    }

    @Override
    public Optional<Usuario> buscarPorId(Integer id) {
        return Optional.ofNullable(usuarios.get(id));
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarios.values().stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    @Override
    public List<Usuario> listarTodos() {
        return new ArrayList<>(usuarios.values());
    }

    @Override
    public void deletar(Integer id) {
        usuarios.remove(id);
    }
}
