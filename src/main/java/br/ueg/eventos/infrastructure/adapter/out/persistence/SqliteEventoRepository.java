package br.ueg.eventos.infrastructure.adapter.out.persistence;

import br.ueg.eventos.application.port.out.EventoRepositoryPort;
import br.ueg.eventos.domain.model.Evento;
import br.ueg.eventos.domain.model.Periodo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteEventoRepository implements EventoRepositoryPort {

    private static final String INSERT = """
            INSERT INTO eventos (
                titulo,
                descricao,
                inicio,
                fim,
                capacidade_maxima,
                inscricoes_abertas,
                deletado,
                certificado,
                frequencia_minima
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String UPDATE = """
            UPDATE eventos
            SET titulo = ?,
                descricao = ?,
                inicio = ?,
                fim = ?,
                capacidade_maxima = ?,
                inscricoes_abertas = ?,
                deletado = ?,
                certificado = ?,
                frequencia_minima = ?
            WHERE id = ?
            """;

    private final SqliteConnectionFactory connectionFactory;

    public SqliteEventoRepository(
            SqliteConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Evento salvar(Evento evento) {
        if (evento == null) {
            throw new IllegalArgumentException(
                    "O evento não pode ser nulo."
            );
        }

        try (Connection connection =
                     connectionFactory.abrirConexao()) {

            if (evento.getId() == null) {
                return inserir(connection, evento);
            }

            atualizar(connection, evento);

            return buscarPorId(connection, evento.getId())
                    .orElseThrow(() -> new IllegalStateException(
                            "O evento salvo não foi encontrado."
                    ));

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Erro ao salvar o evento no SQLite.",
                    e
            );
        }
    }

    @Override
    public Optional<Evento> buscarPorId(Long id) {
        if (id == null || id <= 0) {
            return Optional.empty();
        }

        try (Connection connection =
                     connectionFactory.abrirConexao()) {
            return buscarPorId(connection, id);
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Erro ao buscar evento no SQLite.",
                    e
            );
        }
    }

    @Override
    public List<Evento> listarTodos() {
        String sql = "SELECT * FROM eventos ORDER BY id";

        try (Connection connection =
                     connectionFactory.abrirConexao();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            List<Evento> eventos = new ArrayList<>();

            while (resultSet.next()) {
                eventos.add(mapearEvento(resultSet));
            }

            return eventos;

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Erro ao listar eventos no SQLite.",
                    e
            );
        }
    }

    @Override
    public void deletar(Long id) {
        if (id == null || id <= 0) {
            return;
        }

        String sql = "UPDATE eventos SET deletado = 1 WHERE id = ?";

        try (Connection connection = connectionFactory.abrirConexao();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Erro ao deletar evento no SQLite.",
                    e
            );
        }
    }

    private Evento inserir(
            Connection connection,
            Evento evento) throws SQLException {

        try (PreparedStatement statement =
                     connection.prepareStatement(INSERT)) {
            preencherParametros(statement, evento);
            statement.executeUpdate();
        }

        long id;

        try (Statement statement = connection.createStatement();
             ResultSet resultSet =
                     statement.executeQuery("SELECT last_insert_rowid()")) {

            if (!resultSet.next()) {
                throw new SQLException(
                        "Não foi possível obter o id do evento inserido."
                );
            }

            id = resultSet.getLong(1);
        }

        return buscarPorId(connection, id)
                .orElseThrow(() -> new SQLException(
                        "O evento inserido não foi encontrado."
                ));
    }

    private void atualizar(
            Connection connection,
            Evento evento) throws SQLException {

        try (PreparedStatement statement =
                     connection.prepareStatement(UPDATE)) {

            preencherParametros(statement, evento);
            statement.setLong(10, evento.getId());

            int linhasAtualizadas = statement.executeUpdate();

            if (linhasAtualizadas == 0) {
                throw new SQLException(
                        "Não existe evento com id " + evento.getId()
                );
            }
        }
    }

    private void preencherParametros(
            PreparedStatement statement,
            Evento evento) throws SQLException {

        statement.setString(1, evento.getTitulo());
        statement.setString(2, evento.getDescricao());
        statement.setString(
                3,
                evento.getPeriodo().getInicio().toString()
        );
        statement.setString(
                4,
                evento.getPeriodo().getFim().toString()
        );
        statement.setInt(5, evento.getCapacidadeMaxima());
        statement.setInt(
                6,
                evento.isInscricoesAbertas() ? 1 : 0
        );
        statement.setInt(7, evento.isDeletado() ? 1 : 0);
        statement.setInt(8, evento.isCertificado() ? 1 : 0);
        statement.setInt(9, evento.getFrequenciaMinima());
    }

    private Optional<Evento> buscarPorId(
            Connection connection,
            Long id) throws SQLException {

        String sql = "SELECT * FROM eventos WHERE id = ?";

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {
            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }

                return Optional.of(mapearEvento(resultSet));
            }
        }
    }

    private Evento mapearEvento(ResultSet resultSet)
            throws SQLException {

        LocalDateTime inicio = LocalDateTime.parse(
                resultSet.getString("inicio")
        );
        LocalDateTime fim = LocalDateTime.parse(
                resultSet.getString("fim")
        );

        return new Evento(
                resultSet.getLong("id"),
                resultSet.getString("titulo"),
                resultSet.getString("descricao"),
                new Periodo(inicio, fim),
                resultSet.getInt("capacidade_maxima"),
                resultSet.getInt("inscricoes_abertas") == 1,
                resultSet.getInt("deletado") == 1,
                resultSet.getInt("certificado") == 1,
                resultSet.getInt("frequencia_minima")
        );
    }
}
