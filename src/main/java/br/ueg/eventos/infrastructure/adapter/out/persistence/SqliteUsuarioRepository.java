package br.ueg.eventos.infrastructure.adapter.out.persistence;

import br.ueg.eventos.application.port.out.UsuarioRepositoryPort;
import br.ueg.eventos.domain.model.Perfil;
import br.ueg.eventos.domain.model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteUsuarioRepository extends SqliteRepositorySupport
        implements UsuarioRepositoryPort {

    private static final String INSERT = """
            INSERT INTO usuarios (nome, email, senha_hash, perfil)
            VALUES (?, ?, ?, ?)
            """;

    private static final String UPDATE = """
            UPDATE usuarios
            SET nome = ?, email = ?, senha_hash = ?, perfil = ?
            WHERE id = ?
            """;

    public SqliteUsuarioRepository(
            SqliteConnectionFactory connectionFactory) {
        super(connectionFactory);
    }

    @Override
    public Usuario salvar(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("O usuário não pode ser nulo.");
        }

        try (Connection connection = connectionFactory.abrirConexao()) {
            if (usuario.getId() == null) {
                try (PreparedStatement statement =
                             connection.prepareStatement(INSERT)) {
                    preencherParametros(statement, usuario);
                    statement.executeUpdate();
                }

                int id = Math.toIntExact(ultimoIdInserido(connection));
                return buscarPorId(connection, id)
                        .orElseThrow(() -> new SQLException(
                                "O usuário inserido não foi encontrado."
                        ));
            }

            try (PreparedStatement statement =
                         connection.prepareStatement(UPDATE)) {
                preencherParametros(statement, usuario);
                statement.setInt(5, usuario.getId());
                if (statement.executeUpdate() == 0) {
                    throw new SQLException(
                            "Não existe usuário com id " + usuario.getId()
                    );
                }
            }

            return buscarPorId(connection, usuario.getId())
                    .orElseThrow(() -> new SQLException(
                            "O usuário atualizado não foi encontrado."
                    ));
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Erro ao salvar usuário no SQLite.", e
            );
        }
    }

    @Override
    public Optional<Usuario> buscarPorId(Integer id) {
        if (id == null || id <= 0) {
            return Optional.empty();
        }
        try (Connection connection = connectionFactory.abrirConexao()) {
            return buscarPorId(connection, id);
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Erro ao buscar usuário no SQLite.", e
            );
        }
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }

        String sql = "SELECT * FROM usuarios WHERE email = ?";
        try (Connection connection = connectionFactory.abrirConexao();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next()
                        ? Optional.of(mapearUsuario(resultSet))
                        : Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Erro ao buscar usuário por e-mail no SQLite.", e
            );
        }
    }

    @Override
    public List<Usuario> listarTodos() {
        String sql = "SELECT * FROM usuarios ORDER BY id";
        try (Connection connection = connectionFactory.abrirConexao();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<Usuario> usuarios = new ArrayList<>();
            while (resultSet.next()) {
                usuarios.add(mapearUsuario(resultSet));
            }
            return usuarios;
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Erro ao listar usuários no SQLite.", e
            );
        }
    }

    @Override
    public void deletar(Integer id) {
        if (id == null || id <= 0) {
            return;
        }

        try (Connection connection = connectionFactory.abrirConexao();
             PreparedStatement statement = connection.prepareStatement(
                     "DELETE FROM usuarios WHERE id = ?")) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Erro ao excluir usuário no SQLite.", e
            );
        }
    }

    private Optional<Usuario> buscarPorId(
            Connection connection,
            Integer id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM usuarios WHERE id = ?")) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next()
                        ? Optional.of(mapearUsuario(resultSet))
                        : Optional.empty();
            }
        }
    }

    private void preencherParametros(
            PreparedStatement statement,
            Usuario usuario) throws SQLException {
        statement.setString(1, usuario.getNome());
        statement.setString(2, usuario.getEmail());
        statement.setString(3, usuario.getSenhaHash());
        statement.setString(4, usuario.getPerfil().name());
    }

    private Usuario mapearUsuario(ResultSet resultSet)
            throws SQLException {
        return new Usuario(
                resultSet.getInt("id"),
                resultSet.getString("nome"),
                resultSet.getString("email"),
                resultSet.getString("senha_hash"),
                Perfil.valueOf(resultSet.getString("perfil"))
        );
    }
}
