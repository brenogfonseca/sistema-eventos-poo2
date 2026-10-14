package br.ueg.eventos.infrastructure.adapter.out.persistence;

import br.ueg.eventos.application.port.out.TrilhaRepositoryPort;
import br.ueg.eventos.domain.model.Trilha;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Adaptador que salva trilhas no banco SQLite. */
public class SqliteTrilhaRepository implements TrilhaRepositoryPort {

    private final SqliteConnectionFactory connectionFactory;

    public SqliteTrilhaRepository(SqliteConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Trilha salvar(Trilha trilha) {
        boolean novaTrilha = trilha.getId() == null;
        String sql = novaTrilha
                ? "INSERT INTO trilhas (nome) VALUES (?)"
                : "UPDATE trilhas SET nome = ? WHERE id = ?";

        try (Connection connection = connectionFactory.abrirConexao();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, trilha.getNome());

            if (!novaTrilha) {
                statement.setLong(2, trilha.getId());
            }

            int linhasAfetadas = statement.executeUpdate();

            if (!novaTrilha && linhasAfetadas == 0) {
                throw new SQLException("Trilha não encontrada para atualização.");
            }

            if (novaTrilha) {
                // Busca o ID gerado pelo SQLite.
                try (Statement idStatement = connection.createStatement();
                     ResultSet resultSet =
                             idStatement.executeQuery("SELECT last_insert_rowid()")) {
                    if (!resultSet.next()) {
                        throw new SQLException(
                                "Não foi possível obter o ID da trilha."
                        );
                    }

                    return new Trilha(resultSet.getLong(1), trilha.getNome());
                }
            }

            return trilha;
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Erro ao salvar trilha no SQLite.", e
            );
        }
    }

    @Override
    public Optional<Trilha> buscarPorId(Long id) {
        String sql = "SELECT id, nome FROM trilhas WHERE id = ?";

        try (Connection connection = connectionFactory.abrirConexao();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }

                return Optional.of(new Trilha(
                        resultSet.getLong("id"),
                        resultSet.getString("nome")
                ));
            }
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Erro ao buscar trilha no SQLite.", e
            );
        }
    }

    @Override
    public List<Trilha> listarTodos() {
        String sql = "SELECT id, nome FROM trilhas ORDER BY nome";

        try (Connection connection = connectionFactory.abrirConexao();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<Trilha> trilhas = new ArrayList<>();

            while (resultSet.next()) {
                trilhas.add(new Trilha(
                        resultSet.getLong("id"),
                        resultSet.getString("nome")
                ));
            }

            return trilhas;
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Erro ao listar trilhas no SQLite.", e
            );
        }
    }

    @Override
    public void deletar(Long id) {
        String sql = "DELETE FROM trilhas WHERE id = ?";

        try (Connection connection = connectionFactory.abrirConexao();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Erro ao deletar trilha no SQLite.", e
            );
        }
    }
}