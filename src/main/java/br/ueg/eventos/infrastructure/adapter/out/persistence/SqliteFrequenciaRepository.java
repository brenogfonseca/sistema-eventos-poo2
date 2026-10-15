package br.ueg.eventos.infrastructure.adapter.out.persistence;

import br.ueg.eventos.application.port.out.FrequenciaRepositoryPort;
import br.ueg.eventos.domain.model.Frequencia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteFrequenciaRepository extends SqliteRepositorySupport
        implements FrequenciaRepositoryPort {

    private static final String INSERT = """
            INSERT INTO frequencias (
                data_hora, origem, responsavel_id,
                presente, inscricao_id, tipo
            ) VALUES (?, ?, ?, ?, ?, ?)
            """;

    private static final String UPDATE = """
            UPDATE frequencias
            SET data_hora = ?, origem = ?, responsavel_id = ?,
                presente = ?, inscricao_id = ?, tipo = ?
            WHERE id = ?
            """;

    public SqliteFrequenciaRepository(
            SqliteConnectionFactory connectionFactory) {
        super(connectionFactory);
    }

    @Override
    public Frequencia salvar(Frequencia frequencia) {
        if (frequencia == null) {
            throw new IllegalArgumentException(
                    "A frequência não pode ser nula."
            );
        }

        try (Connection connection = connectionFactory.abrirConexao()) {
            if (frequencia.getId() == null) {
                try (PreparedStatement statement =
                             connection.prepareStatement(INSERT)) {
                    preencherParametros(statement, frequencia);
                    statement.executeUpdate();
                }
                int id = Math.toIntExact(ultimoIdInserido(connection));
                return buscarPorId(connection, id)
                        .orElseThrow(() -> new SQLException(
                                "A frequência inserida não foi encontrada."
                        ));
            }

            try (PreparedStatement statement =
                         connection.prepareStatement(UPDATE)) {
                preencherParametros(statement, frequencia);
                statement.setInt(7, frequencia.getId());
                if (statement.executeUpdate() == 0) {
                    throw new SQLException(
                            "Não existe frequência com id " + frequencia.getId()
                    );
                }
            }
            return buscarPorId(connection, frequencia.getId())
                    .orElseThrow(() -> new SQLException(
                            "A frequência atualizada não foi encontrada."
                    ));
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Erro ao salvar frequência no SQLite.", e
            );
        }
    }

    @Override
    public Optional<Frequencia> buscarPorId(Integer id) {
        if (id == null || id <= 0) {
            return Optional.empty();
        }
        try (Connection connection = connectionFactory.abrirConexao()) {
            return buscarPorId(connection, id);
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Erro ao buscar frequência no SQLite.", e
            );
        }
    }

    @Override
    public List<Frequencia> listarPorInscricao(Integer idInscricao) {
        if (idInscricao == null || idInscricao <= 0) {
            return List.of();
        }

        String sql = """
                SELECT *
                FROM frequencias
                WHERE inscricao_id = ?
                ORDER BY data_hora, id
                """;
        try (Connection connection = connectionFactory.abrirConexao();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idInscricao);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Frequencia> frequencias = new ArrayList<>();
                while (resultSet.next()) {
                    frequencias.add(mapearFrequencia(resultSet));
                }
                return frequencias;
            }
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Erro ao listar frequências no SQLite.", e
            );
        }
    }

    private Optional<Frequencia> buscarPorId(
            Connection connection,
            Integer id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM frequencias WHERE id = ?")) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next()
                        ? Optional.of(mapearFrequencia(resultSet))
                        : Optional.empty();
            }
        }
    }

    private void preencherParametros(
            PreparedStatement statement,
            Frequencia frequencia) throws SQLException {
        statement.setString(1, frequencia.getDataHora().toString());
        statement.setString(2, frequencia.getOrigem());
        if (frequencia.getIdResponsavel() == null) {
            statement.setNull(3, Types.INTEGER);
        } else {
            statement.setInt(3, frequencia.getIdResponsavel());
        }
        statement.setInt(4, frequencia.isPresente() ? 1 : 0);
        statement.setInt(5, frequencia.getIdInscricao());
        statement.setString(6, frequencia.getTipo());
    }

    private Frequencia mapearFrequencia(ResultSet resultSet)
            throws SQLException {
        int responsavel = resultSet.getInt("responsavel_id");
        Integer idResponsavel = resultSet.wasNull() ? null : responsavel;

        return new Frequencia(
                resultSet.getInt("id"),
                LocalDateTime.parse(resultSet.getString("data_hora")),
                resultSet.getString("origem"),
                idResponsavel,
                resultSet.getInt("presente") == 1,
                resultSet.getInt("inscricao_id"),
                resultSet.getString("tipo")
        );
    }
}
