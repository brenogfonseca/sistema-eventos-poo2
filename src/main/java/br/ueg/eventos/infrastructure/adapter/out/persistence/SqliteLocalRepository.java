package br.ueg.eventos.infrastructure.adapter.out.persistence;

import br.ueg.eventos.application.port.out.LocalRepositoryPort;
import br.ueg.eventos.domain.model.Local;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteLocalRepository extends SqliteRepositorySupport
        implements LocalRepositoryPort {

    public SqliteLocalRepository(
            SqliteConnectionFactory connectionFactory) {
        super(connectionFactory);
    }

    @Override
    public Local salvar(Local local) {
        if (local == null) {
            throw new IllegalArgumentException("O local não pode ser nulo.");
        }

        try (Connection connection = connectionFactory.abrirConexao()) {
            if (local.getId() == null) {
                try (PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO locais (nome) VALUES (?)")) {
                    statement.setString(1, local.getNome());
                    statement.executeUpdate();
                }
                long id = ultimoIdInserido(connection);
                return buscarPorId(connection, id)
                        .orElseThrow(() -> new SQLException(
                                "O local inserido não foi encontrado."
                        ));
            }

            try (PreparedStatement statement = connection.prepareStatement(
                    "UPDATE locais SET nome = ? WHERE id = ?")) {
                statement.setString(1, local.getNome());
                statement.setLong(2, local.getId());
                if (statement.executeUpdate() == 0) {
                    throw new SQLException(
                            "Não existe local com id " + local.getId()
                    );
                }
            }
            return buscarPorId(connection, local.getId())
                    .orElseThrow(() -> new SQLException(
                            "O local atualizado não foi encontrado."
                    ));
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Erro ao salvar local no SQLite.", e
            );
        }
    }

    @Override
    public Optional<Local> buscarPorId(Long id) {
        if (id == null || id <= 0) {
            return Optional.empty();
        }
        try (Connection connection = connectionFactory.abrirConexao()) {
            return buscarPorId(connection, id);
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Erro ao buscar local no SQLite.", e
            );
        }
    }

    @Override
    public List<Local> listarTodos() {
        String sql = "SELECT id, nome FROM locais ORDER BY id";
        try (Connection connection = connectionFactory.abrirConexao();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<Local> locais = new ArrayList<>();
            while (resultSet.next()) {
                locais.add(mapearLocal(resultSet));
            }
            return locais;
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Erro ao listar locais no SQLite.", e
            );
        }
    }

    @Override
    public void deletar(Long id) {
        if (id == null || id <= 0) {
            return;
        }
        try (Connection connection = connectionFactory.abrirConexao();
             PreparedStatement statement = connection.prepareStatement(
                     "DELETE FROM locais WHERE id = ?")) {
            statement.setLong(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Erro ao excluir local no SQLite.", e
            );
        }
    }

    private Optional<Local> buscarPorId(
            Connection connection,
            Long id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT id, nome FROM locais WHERE id = ?")) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next()
                        ? Optional.of(mapearLocal(resultSet))
                        : Optional.empty();
            }
        }
    }

    private Local mapearLocal(ResultSet resultSet) throws SQLException {
        return new Local(
                resultSet.getLong("id"),
                resultSet.getString("nome")
        );
    }
}
