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
        Path caminhoEscolhido = resolverCaminhoBanco();
        Path caminhoFinal = caminhoEscolhido;

        try {
            Path diretorio = caminhoEscolhido.toAbsolutePath().getParent();
            if (diretorio != null) {
                Files.createDirectories(diretorio);
            }
        } catch (Exception e) {
            // Em caso de restrição de permissão no diretório padrão, utiliza fallback no diretório corrente
            caminhoFinal = Path.of("eventos.db").toAbsolutePath().normalize();
        }

        this.databasePath = caminhoFinal;
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
