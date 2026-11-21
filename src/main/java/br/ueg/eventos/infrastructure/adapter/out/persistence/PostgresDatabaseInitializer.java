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
import java.util.List;

public class PostgresDatabaseInitializer {

    private static final List<Migracao> MIGRACOES = List.of(
            new Migracao(
                    1,
                    "Criacao inicial de eventos e atividades",
                    "/db/migration/postgresql/V1__criar_tabelas.sql"
            ),
            new Migracao(
                    2,
                    "Criacao das demais tabelas do sistema",
                    "/db/migration/postgresql/V2__criar_tabelas_restantes.sql"
            ),
            new Migracao(
                    3,
                    "Criacao da tabela de vinculos de organizadores",
                    "/db/migration/postgresql/V3__criar_vinculos_organizadores.sql"
            ),
            new Migracao(
                    4,
                    "Reformulacao da inscricao em evento e escolha de atividades",
                    "/db/migration/postgresql/V4__reformular_inscricoes_evento.sql"
            ),
            new Migracao(
                    5,
                    "Complemento do modelo para requisitos do sistema",
                    "/db/migration/postgresql/V5__completar_modelo_requisitos.sql"
            ),
            new Migracao(
                    6,
                    "Prazo configurável para cancelamento de inscrição",
                    "/db/migration/postgresql/V6__prazo_cancelamento_evento.sql"
            ),
            new Migracao(
                    7,
                    "Vínculo persistente de local usado pela atividade",
                    "/db/migration/postgresql/V7__vinculo_local_atividade.sql"
            ),
            new Migracao(
                    8,
                    "Emissão de certificados para palestrantes e apresentadores",
                    "/db/migration/postgresql/V8__certificados_pessoas_programacao.sql"
            )
    );

    private final PostgresConnectionFactory connectionFactory;

    public PostgresDatabaseInitializer(
            PostgresConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public void inicializar() {
        try (Connection connection =
                     connectionFactory.abrirConexao()) {

            criarTabelaDeMigracoes(connection);

            for (Migracao migracao : MIGRACOES) {
                if (!migracaoJaAplicada(connection, migracao.versao())) {
                    aplicarMigracao(connection, migracao);
                }
            }

        } catch (SQLException | IOException e) {
            throw new IllegalStateException(
                    "Não foi possível inicializar o banco PostgreSQL.",
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

    private void aplicarMigracao(
            Connection connection,
            Migracao migracao) throws SQLException, IOException {
        String sqlMigracao = lerMigracao(migracao.arquivo());
        connection.setAutoCommit(false);

        try {
            executarComandos(connection, sqlMigracao);
            registrarMigracao(connection, migracao);
            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException erroRollback) {
                e.addSuppressed(erroRollback);
            }
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    private String lerMigracao(String arquivo) throws IOException {
        try (InputStream inputStream =
                     getClass().getResourceAsStream(arquivo)) {

            if (inputStream == null) {
                throw new IOException(
                        "Arquivo de migração não encontrado: "
                                + arquivo
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
            Connection connection,
            Migracao migracao) throws SQLException {

        String sql = """
                INSERT INTO schema_migrations (
                    version,
                    description,
                    applied_at
                ) VALUES (?, ?, ?)
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {
            statement.setInt(1, migracao.versao());
            statement.setString(2, migracao.descricao());
            statement.setString(3, LocalDateTime.now().toString());
            statement.executeUpdate();
        }
    }

    private record Migracao(int versao, String descricao, String arquivo) {
    }
}
