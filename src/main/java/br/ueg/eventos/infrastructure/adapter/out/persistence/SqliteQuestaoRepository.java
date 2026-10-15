package br.ueg.eventos.infrastructure.adapter.out.persistence;

import br.ueg.eventos.application.port.out.QuestaoRepositoryPort;
import br.ueg.eventos.domain.model.Questao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteQuestaoRepository extends SqliteRepositorySupport
        implements QuestaoRepositoryPort {

    private static final String INSERT = """
            INSERT INTO questoes (enunciado, tipo, questionario_id)
            VALUES (?, ?, ?)
            """;

    private static final String UPDATE = """
            UPDATE questoes
            SET enunciado = ?, tipo = ?, questionario_id = ?
            WHERE id = ?
            """;

    public SqliteQuestaoRepository(
            SqliteConnectionFactory connectionFactory) {
        super(connectionFactory);
    }

    @Override
    public Questao salvar(Questao questao) {
        if (questao == null) {
            throw new IllegalArgumentException("A questão não pode ser nula.");
        }

        try (Connection connection = connectionFactory.abrirConexao()) {
            if (questao.getId() == null) {
                try (PreparedStatement statement =
                             connection.prepareStatement(INSERT)) {
                    preencherParametros(statement, questao);
                    statement.executeUpdate();
                }
                int id = Math.toIntExact(ultimoIdInserido(connection));
                return buscarPorId(connection, id)
                        .orElseThrow(() -> new SQLException(
                                "A questão inserida não foi encontrada."
                        ));
            }

            try (PreparedStatement statement =
                         connection.prepareStatement(UPDATE)) {
                preencherParametros(statement, questao);
                statement.setInt(4, questao.getId());
                if (statement.executeUpdate() == 0) {
                    throw new SQLException(
                            "Não existe questão com id " + questao.getId()
                    );
                }
            }
            return buscarPorId(connection, questao.getId())
                    .orElseThrow(() -> new SQLException(
                            "A questão atualizada não foi encontrada."
                    ));
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Erro ao salvar questão no SQLite.", e
            );
        }
    }

    @Override
    public Optional<Questao> buscarPorId(Long id) {
        if (id == null || id <= 0) {
            return Optional.empty();
        }
        try (Connection connection = connectionFactory.abrirConexao()) {
            return buscarPorId(connection, id);
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Erro ao buscar questão no SQLite.", e
            );
        }
    }

    @Override
    public List<Questao> buscarPorIdQuestionario(Long idQuestionario) {
        if (idQuestionario == null || idQuestionario <= 0) {
            return List.of();
        }

        String sql = """
                SELECT *
                FROM questoes
                WHERE questionario_id = ?
                ORDER BY id
                """;
        try (Connection connection = connectionFactory.abrirConexao();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, idQuestionario);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Questao> questoes = new ArrayList<>();
                while (resultSet.next()) {
                    questoes.add(mapearQuestao(resultSet));
                }
                return questoes;
            }
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Erro ao listar questões do questionário no SQLite.", e
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
                     "DELETE FROM questoes WHERE id = ?")) {
            statement.setLong(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Erro ao excluir questão no SQLite.", e
            );
        }
    }

    private Optional<Questao> buscarPorId(
            Connection connection,
            long id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM questoes WHERE id = ?")) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next()
                        ? Optional.of(mapearQuestao(resultSet))
                        : Optional.empty();
            }
        }
    }

    private void preencherParametros(
            PreparedStatement statement,
            Questao questao) throws SQLException {
        statement.setString(1, questao.getEnunciado());
        statement.setString(2, questao.getTipo().name());
        statement.setInt(3, questao.getIdQuestionario());
    }

    private Questao mapearQuestao(ResultSet resultSet)
            throws SQLException {
        return new Questao(
                resultSet.getInt("id"),
                resultSet.getString("enunciado"),
                Questao.TipoQuestao.valueOf(resultSet.getString("tipo")),
                resultSet.getInt("questionario_id")
        );
    }
}
