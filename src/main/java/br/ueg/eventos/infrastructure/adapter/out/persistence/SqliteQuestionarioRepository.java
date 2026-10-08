package br.ueg.eventos.infrastructure.adapter.out.persistence;

import br.ueg.eventos.application.port.out.QuestionarioRepositoryPort;
import br.ueg.eventos.domain.model.Questionario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class SqliteQuestionarioRepository extends SqliteRepositorySupport
        implements QuestionarioRepositoryPort {

    private static final String INSERT = """
            INSERT INTO questionarios (atividade_id, titulo)
            VALUES (?, ?)
            """;

    private static final String UPDATE = """
            UPDATE questionarios
            SET atividade_id = ?, titulo = ?
            WHERE id = ?
            """;

    public SqliteQuestionarioRepository(
            SqliteConnectionFactory connectionFactory) {
        super(connectionFactory);
    }

    @Override
    public Questionario salvar(Questionario questionario) {
        if (questionario == null) {
            throw new IllegalArgumentException(
                    "O questionário não pode ser nulo."
            );
        }

        try (Connection connection = connectionFactory.abrirConexao()) {
            if (questionario.getId() == null) {
                try (PreparedStatement statement =
                             connection.prepareStatement(INSERT)) {
                    preencherParametros(statement, questionario);
                    statement.executeUpdate();
                }
                int id = Math.toIntExact(ultimoIdInserido(connection));
                return buscarPorId(connection, id)
                        .orElseThrow(() -> new SQLException(
                                "O questionário inserido não foi encontrado."
                        ));
            }

            try (PreparedStatement statement =
                         connection.prepareStatement(UPDATE)) {
                preencherParametros(statement, questionario);
                statement.setInt(3, questionario.getId());
                if (statement.executeUpdate() == 0) {
                    throw new SQLException(
                            "Não existe questionário com id "
                                    + questionario.getId()
                    );
                }
            }
            return buscarPorId(connection, questionario.getId())
                    .orElseThrow(() -> new SQLException(
                            "O questionário atualizado não foi encontrado."
                    ));
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Erro ao salvar questionário no SQLite.", e
            );
        }
    }

    @Override
    public Optional<Questionario> buscarPorId(Long id) {
        if (id == null || id <= 0) {
            return Optional.empty();
        }
        try (Connection connection = connectionFactory.abrirConexao()) {
            return buscarPorId(connection, id);
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Erro ao buscar questionário no SQLite.", e
            );
        }
    }

    @Override
    public Optional<Questionario> buscarPorIdAtividade(Long idAtividade) {
        if (idAtividade == null || idAtividade <= 0) {
            return Optional.empty();
        }

        String sql = "SELECT * FROM questionarios WHERE atividade_id = ?";
        try (Connection connection = connectionFactory.abrirConexao();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, idAtividade);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next()
                        ? Optional.of(mapearQuestionario(resultSet))
                        : Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Erro ao buscar questionário por atividade no SQLite.", e
            );
        }
    }

    @Override
    public void excluir(Long id) {
        if (id == null || id <= 0) {
            return;
        }
        try (Connection connection = connectionFactory.abrirConexao();
             PreparedStatement statement = connection.prepareStatement(
                     "DELETE FROM questionarios WHERE id = ?")) {
            statement.setLong(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Erro ao excluir questionário no SQLite.", e
            );
        }
    }

    private Optional<Questionario> buscarPorId(
            Connection connection,
            long id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM questionarios WHERE id = ?")) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next()
                        ? Optional.of(mapearQuestionario(resultSet))
                        : Optional.empty();
            }
        }
    }

    private void preencherParametros(
            PreparedStatement statement,
            Questionario questionario) throws SQLException {
        statement.setInt(1, questionario.getIdAtividade());
        statement.setString(2, questionario.getTitulo());
    }

    private Questionario mapearQuestionario(ResultSet resultSet)
            throws SQLException {
        return new Questionario(
                resultSet.getInt("id"),
                resultSet.getInt("atividade_id"),
                resultSet.getString("titulo")
        );
    }
}
