package br.ueg.eventos.infrastructure.adapter.out.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;

public class SqliteDatabaseInitializer {

    private static final int VERSAO_INICIAL = 1;
    private static final String ARQUIVO_MIGRACAO =
            "/db/migration/V1__criar_tabelas.sql";

    private final SqliteConnectionFactory connectionFactory;

    public SqliteDatabaseInitializer(
            SqliteConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public void inicializar() {
        try (Connection connection =
                     connectionFactory.abrirConexao()) {

            criarTabelaDeMigracoes(connection);

            if (migracaoJaAplicada(connection, VERSAO_INICIAL)) {
                return;
            }

            String sqlMigracao = lerMigracao();
            connection.setAutoCommit(false);

            try {
                executarComandos(connection, sqlMigracao);
                registrarMigracao(connection);
                connection.commit();
            } catch (SQLException e) {
                try {
                    connection.rollback();
                } catch (SQLException erroRollback) {
                    e.addSuppressed(erroRollback);
                }
                throw e;
            }

        } catch (SQLException | IOException e) {
            throw new IllegalStateException(
                    "Não foi possível inicializar o banco SQLite.",
                    e
            );
        }
    }

    private void criarTabelaDeMigracoes(
            Connection connection) throws SQLException {

        String sql = """
                CREATE TABLE IF NOT EXISTS schema_migrations (
                    version INTEGER PRIMARY KEY,
                    description TEXT NOT NULL,
                    applied_at TEXT NOT NULL
                )
                """;

        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    private boolean migracaoJaAplicada(
            Connection connection,
            int versao) throws SQLException {

        String sql = """
                SELECT 1
                FROM schema_migrations
                WHERE version = ?
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {
            statement.setInt(1, versao);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private String lerMigracao() throws IOException {
        try (InputStream inputStream =
                     getClass().getResourceAsStream(ARQUIVO_MIGRACAO)) {

            if (inputStream == null) {
                throw new IOException(
                        "Arquivo de migração não encontrado: "
                                + ARQUIVO_MIGRACAO
                );
            }

            return new String(
                    inputStream.readAllBytes(),
                    StandardCharsets.UTF_8
            );
        }
    }

    private void executarComandos(
            Connection connection,
            String sqlMigracao) throws SQLException {

        String[] comandos = sqlMigracao.split(";");

        try (Statement statement = connection.createStatement()) {
            for (String comando : comandos) {
                if (!comando.isBlank()) {
                    statement.execute(comando.trim());
                }
            }
        }
    }

    private void registrarMigracao(
            Connection connection) throws SQLException {

        String sql = """
                INSERT INTO schema_migrations (
                    version,
                    description,
                    applied_at
                ) VALUES (?, ?, ?)
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {
            statement.setInt(1, VERSAO_INICIAL);
            statement.setString(
                    2,
                    "Criacao inicial de eventos e atividades"
            );
            statement.setString(3, LocalDateTime.now().toString());
            statement.executeUpdate();
        }
    }
}
