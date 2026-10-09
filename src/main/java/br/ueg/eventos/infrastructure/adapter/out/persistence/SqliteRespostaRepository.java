package br.ueg.eventos.infrastructure.adapter.out.persistence;

import br.ueg.eventos.application.port.out.RespostaRepositoryPort;
import br.ueg.eventos.domain.model.Resposta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteRespostaRepository extends SqliteRepositorySupport
        implements RespostaRepositoryPort {

    private static final String INSERT = """
            INSERT INTO respostas (questao_id, usuario_id, valor)
            VALUES (?, ?, ?)
            """;

    private static final String UPDATE = """
            UPDATE respostas
            SET questao_id = ?, usuario_id = ?, valor = ?
            WHERE id = ?
            """;

    public SqliteRespostaRepository(
            SqliteConnectionFactory connectionFactory) {
        super(connectionFactory);
    }

    @Override
    public Resposta salvar(Resposta resposta) {
        if (resposta == null) {
            throw new IllegalArgumentException("A resposta não pode ser nula.");
        }

        try (Connection connection = connectionFactory.abrirConexao()) {
            if (resposta.getId() == null) {
                try (PreparedStatement statement =
                             connection.prepareStatement(INSERT)) {
                    preencherParametros(statement, resposta);
                    statement.executeUpdate();
                }
                int id = Math.toIntExact(ultimoIdInserido(connection));
                return buscarPorId(connection, id)
                        .orElseThrow(() -> new SQLException(
                                "A resposta inserida não foi encontrada."
                        ));
            }

            try (PreparedStatement statement =
                         connection.prepareStatement(UPDATE)) {
                preencherParametros(statement, resposta);
                statement.setInt(4, resposta.getId());
                if (statement.executeUpdate() == 0) {
                    throw new SQLException(
                            "Não existe resposta com id " + resposta.getId()
                    );
                }
            }
            return buscarPorId(connection, resposta.getId())
                    .orElseThrow(() -> new SQLException(
                            "A resposta atualizada não foi encontrada."
                    ));
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Erro ao salvar resposta no SQLite.", e
            );
        }
    }

    @Override
    public Optional<Resposta> buscarPorId(Long id) {
        if (id == null || id <= 0) {
            return Optional.empty();
        }
        try (Connection connection = connectionFactory.abrirConexao()) {
            return buscarPorId(connection, id);
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Erro ao buscar resposta no SQLite.", e
            );
        }
    }

    @Override
    public List<Resposta> buscarPorIdQuestao(Long idQuestao) {
        if (idQuestao == null || idQuestao <= 0) {
            return List.of();
        }

        String sql = """
                SELECT *
                FROM respostas
                WHERE questao_id = ?
                ORDER BY id
                """;
        try (Connection connection = connectionFactory.abrirConexao();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, idQuestao);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Resposta> respostas = new ArrayList<>();
                while (resultSet.next()) {
                    respostas.add(mapearResposta(resultSet));
                }
                return respostas;
            }
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Erro ao listar respostas da questão no SQLite.", e
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
                     "DELETE FROM respostas WHERE id = ?")) {
            statement.setLong(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Erro ao excluir resposta no SQLite.", e
            );
        }
    }

    private Optional<Resposta> buscarPorId(
            Connection connection,
            long id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM respostas WHERE id = ?")) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next()
                        ? Optional.of(mapearResposta(resultSet))
                        : Optional.empty();
            }
        }
    }

    private void preencherParametros(
            PreparedStatement statement,
            Resposta resposta) throws SQLException {
        statement.setInt(1, resposta.getIdQuestao());
        statement.setInt(2, resposta.getIdUsuario());
        statement.setString(3, resposta.getValor());
    }

    private Resposta mapearResposta(ResultSet resultSet)
            throws SQLException {
        return new Resposta(
                resultSet.getInt("id"),
                resultSet.getInt("questao_id"),
                resultSet.getInt("usuario_id"),
                resultSet.getString("valor")
        );
    }
}
