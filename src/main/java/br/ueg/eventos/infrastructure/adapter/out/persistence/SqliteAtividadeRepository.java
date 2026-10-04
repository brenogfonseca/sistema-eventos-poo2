package br.ueg.eventos.infrastructure.adapter.out.persistence;

import br.ueg.eventos.application.port.out.AtividadeRepositoryPort;
import br.ueg.eventos.domain.model.Atividade;
import br.ueg.eventos.domain.model.Local;
import br.ueg.eventos.domain.model.TipoFrequenciaEnum;
import br.ueg.eventos.domain.model.Trilha;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteAtividadeRepository implements AtividadeRepositoryPort {

    private static final String INSERT = """
            INSERT INTO atividades (
                nome,
                evento_id,
                local_id,
                local_nome,
                hora_inicio,
                hora_fim,
                data,
                controla_vagas,
                vagas,
                tipo,
                tipo_frequencia,
                trilha_id,
                trilha_nome
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String UPDATE = """
            UPDATE atividades
            SET nome = ?,
                evento_id = ?,
                local_id = ?,
                local_nome = ?,
                hora_inicio = ?,
                hora_fim = ?,
                data = ?,
                controla_vagas = ?,
                vagas = ?,
                tipo = ?,
                tipo_frequencia = ?,
                trilha_id = ?,
                trilha_nome = ?
            WHERE id = ?
            """;

    private final SqliteConnectionFactory connectionFactory;

    public SqliteAtividadeRepository(
            SqliteConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Atividade salvar(Atividade atividade) {
        if (atividade == null) {
            throw new IllegalArgumentException(
                    "A atividade não pode ser nula."
            );
        }

        try (Connection connection = connectionFactory.abrirConexao()) {
            if (atividade.getId() == null) {
                return inserir(connection, atividade);
            }

            atualizar(connection, atividade);

            return buscarPorId(connection, atividade.getId())
                    .orElseThrow(() -> new IllegalStateException(
                            "A atividade salva não foi encontrada."
                    ));
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Erro ao salvar atividade no SQLite.",
                    e
            );
        }
    }

    @Override
    public Optional<Atividade> buscarPorId(Long id) {
        if (id == null || id <= 0) {
            return Optional.empty();
        }

        try (Connection connection = connectionFactory.abrirConexao()) {
            return buscarPorId(connection, id);
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Erro ao buscar atividade no SQLite.",
                    e
            );
        }
    }

    @Override
    public List<Atividade> listarPorEvento(Long idEvento) {
        if (idEvento == null || idEvento <= 0) {
            return List.of();
        }

        String sql = """
                SELECT *
                FROM atividades
                WHERE evento_id = ?
                ORDER BY data, hora_inicio, id
                """;

        try (Connection connection = connectionFactory.abrirConexao();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, idEvento);

            try (ResultSet resultSet = statement.executeQuery()) {
                List<Atividade> atividades = new ArrayList<>();

                while (resultSet.next()) {
                    atividades.add(mapearAtividade(resultSet));
                }

                return atividades;
            }
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Erro ao listar atividades do evento no SQLite.",
                    e
            );
        }
    }

    @Override
    public void excluir(Long id) {
        if (id == null || id <= 0) {
            return;
        }

        String sql = "DELETE FROM atividades WHERE id = ?";

        try (Connection connection = connectionFactory.abrirConexao();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Erro ao excluir atividade no SQLite.",
                    e
            );
        }
    }

    private Atividade inserir(
            Connection connection,
            Atividade atividade) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT)) {
            preencherParametros(statement, atividade);
            statement.executeUpdate();
        }

        long id;
        try (Statement statement = connection.createStatement();
             ResultSet resultSet =
                     statement.executeQuery("SELECT last_insert_rowid()")) {
            if (!resultSet.next()) {
                throw new SQLException(
                        "Não foi possível obter o id da atividade inserida."
                );
            }
            id = resultSet.getLong(1);
        }

        return buscarPorId(connection, id)
                .orElseThrow(() -> new SQLException(
                        "A atividade inserida não foi encontrada."
                ));
    }

    private void atualizar(
            Connection connection,
            Atividade atividade) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(UPDATE)) {
            preencherParametros(statement, atividade);
            statement.setLong(14, atividade.getId());

            if (statement.executeUpdate() == 0) {
                throw new SQLException(
                        "Não existe atividade com id " + atividade.getId()
                );
            }
        }
    }

    private void preencherParametros(
            PreparedStatement statement,
            Atividade atividade) throws SQLException {
        statement.setString(1, atividade.getNome());
        statement.setLong(2, atividade.getEventoId());

        Local local = atividade.getLocal();
        if (local == null) {
            statement.setNull(3, java.sql.Types.INTEGER);
            statement.setNull(4, java.sql.Types.VARCHAR);
        } else {
            if (local.getId() == null) {
                statement.setNull(3, java.sql.Types.INTEGER);
            } else {
                statement.setLong(3, local.getId());
            }
            statement.setString(4, local.getNome());
        }

        statement.setString(5, atividade.getHoraInicio().toString());
        statement.setString(6, atividade.getHoraFim().toString());
        statement.setString(7, atividade.getData().toString());
        statement.setInt(8, atividade.isControlaVagas() ? 1 : 0);
        statement.setInt(9, atividade.getVagas());
        statement.setString(10, atividade.getTipo());

        TipoFrequenciaEnum tipoFrequencia = atividade.getTipoFrequencia();
        if (tipoFrequencia == null) {
            statement.setNull(11, java.sql.Types.VARCHAR);
        } else {
            statement.setString(11, tipoFrequencia.name());
        }

        Trilha trilha = atividade.getTrilha();
        if (trilha == null) {
            statement.setNull(12, java.sql.Types.INTEGER);
            statement.setNull(13, java.sql.Types.VARCHAR);
        } else {
            if (trilha.getId() == null) {
                statement.setNull(12, java.sql.Types.INTEGER);
            } else {
                statement.setLong(12, trilha.getId());
            }
            statement.setString(13, trilha.getNome());
        }
    }

    private Optional<Atividade> buscarPorId(
            Connection connection,
            Long id) throws SQLException {
        String sql = "SELECT * FROM atividades WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapearAtividade(resultSet));
            }
        }
    }

    private Atividade mapearAtividade(ResultSet resultSet)
            throws SQLException {
        long localId = resultSet.getLong("local_id");
        boolean localIdNulo = resultSet.wasNull();
        String localNome = resultSet.getString("local_nome");
        Local local = localIdNulo && localNome == null
                ? null
                : new Local(localIdNulo ? null : localId, localNome);

        long trilhaId = resultSet.getLong("trilha_id");
        boolean trilhaIdNulo = resultSet.wasNull();
        String trilhaNome = resultSet.getString("trilha_nome");
        Trilha trilha = trilhaIdNulo && trilhaNome == null
                ? null
                : new Trilha(trilhaIdNulo ? null : trilhaId, trilhaNome);

        String frequencia = resultSet.getString("tipo_frequencia");
        TipoFrequenciaEnum tipoFrequencia = frequencia == null
                ? null
                : TipoFrequenciaEnum.valueOf(frequencia);

        return new Atividade(
                resultSet.getLong("id"),
                resultSet.getString("nome"),
                resultSet.getLong("evento_id"),
                local,
                LocalTime.parse(resultSet.getString("hora_inicio")),
                LocalTime.parse(resultSet.getString("hora_fim")),
                LocalDate.parse(resultSet.getString("data")),
                resultSet.getInt("controla_vagas") == 1,
                resultSet.getInt("vagas"),
                resultSet.getString("tipo"),
                tipoFrequencia,
                trilha
        );
    }
}
