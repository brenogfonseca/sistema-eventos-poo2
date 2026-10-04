package br.ueg.eventos.infrastructure.adapter.out.persistence;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class SqliteConnectionFactory {

    private final Path databasePath;
    private final String jdbcUrl;

    public SqliteConnectionFactory() {
        this.databasePath = resolverCaminhoBanco();

        try {
            Path diretorio = databasePath.toAbsolutePath().getParent();

            if (diretorio != null) {
                Files.createDirectories(diretorio);
            }
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Não foi possível criar a pasta do banco SQLite.",
                    e
            );
        }

        String caminhoJDBC = databasePath
                .toAbsolutePath()
                .toString()
                .replace("\\", "/");

        this.jdbcUrl = "jdbc:sqlite:" + caminhoJDBC;
    }

    public Connection abrirConexao() throws SQLException {
        Connection connection = DriverManager.getConnection(jdbcUrl);

        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
            return connection;
        } catch (SQLException e) {
            connection.close();
            throw e;
        }
    }

    public Path getDatabasePath() {
        return databasePath;
    }

    private Path resolverCaminhoBanco() {
        String caminhoConfigurado =
                System.getenv("EVENTOS_DB_PATH");

        if (caminhoConfigurado != null
                && !caminhoConfigurado.isBlank()) {
            return Path.of(caminhoConfigurado)
                    .toAbsolutePath()
                    .normalize();
        }

        String localAppData = System.getenv("LOCALAPPDATA");

        if (localAppData != null && !localAppData.isBlank()) {
            return Path.of(
                    localAppData,
                    "SistemaEventos",
                    "eventos.db"
            );
        }

        return Path.of(
                System.getProperty("user.home"),
                "SistemaEventos",
                "eventos.db"
        );
    }
}
