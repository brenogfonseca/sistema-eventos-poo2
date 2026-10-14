package br.ueg.eventos.infrastructure.adapter.out.persistence;

import br.ueg.eventos.application.port.out.TipoRepositoryPort;
import br.ueg.eventos.domain.model.Tipo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Adaptador que salva tipos no banco SQLite. */
public class SqliteTipoRepository implements TipoRepositoryPort {

    private final SqliteConnectionFactory connectionFactory;

    public SqliteTipoRepository(SqliteConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Tipo salvar(Tipo tipo) {
        boolean novoTipo = tipo.getId() == null;
        String sql = novoTipo
                ? "INSERT INTO tipos (nome) VALUES (?)"
                : "UPDATE tipos SET nome = ? WHERE id = ?";

        try (Connection connection = connectionFactory.abrirConexao();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, tipo.getNome());

            if (!novoTipo) {
                statement.setLong(2, tipo.getId());
            }

            statement.executeUpdate();

            if (novoTipo) {
                // Busca o ID gerado pelo SQLite.
                try (Statement idStatement = connection.createStatement();
                     ResultSet resultSet =
                             idStatement.executeQuery("SELECT last_insert_rowid()")) {
                    if (!resultSet.next()) {
                        throw new SQLException("Não foi possível obter o ID do tipo.");
                    }
                    return new Tipo(resultSet.getLong(1), tipo.getNome());
                }
            }

            return tipo;
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao salvar tipo no SQLite.", e);
        }
    }

    @Override
    public Optional<Tipo> buscarPorId(Long id) {
        String sql = "SELECT id, nome FROM tipos WHERE id = ?";

        try (Connection connection = connectionFactory.abrirConexao();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }

                return Optional.of(new Tipo(
                        resultSet.getLong("id"),
                        resultSet.getString("nome")
                ));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao buscar tipo no SQLite.", e);
        }
    }

    @Override
    public List<Tipo> listarTodos() {
        String sql = "SELECT id, nome FROM tipos ORDER BY nome";

        try (Connection connection = connectionFactory.abrirConexao();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<Tipo> tipos = new ArrayList<>();

            while (resultSet.next()) {
                tipos.add(new Tipo(
                        resultSet.getLong("id"),
                        resultSet.getString("nome")
                ));
            }

            return tipos;
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao listar tipos no SQLite.", e);
        }
    }

    @Override
    public void deletar(Long id) {
        String sql = "DELETE FROM tipos WHERE id = ?";

        try (Connection connection = connectionFactory.abrirConexao();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao deletar tipo no SQLite.", e);
        }
    }
}