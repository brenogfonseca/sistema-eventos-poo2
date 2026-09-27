package br.ueg.eventos.infrastructure.adapter.out.persistence;

import br.ueg.eventos.application.port.out.UsuarioRepository;
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
public class InMemoryUsuarioRepository implements UsuarioRepository {

    private final Map<Integer, Usuario> usuarios = new ConcurrentHashMap<>();
    private final AtomicInteger idGenerator = new AtomicInteger(1);

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
