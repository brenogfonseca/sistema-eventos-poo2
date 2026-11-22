package br.ueg.eventos.infrastructure.adapter.out.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import br.ueg.eventos.infrastructure.config.ApplicationEnvironment;

/** Creates PostgreSQL connections from environment-based configuration. */
public final class PostgresConnectionFactory {
    private final String url;
    private final String usuario;
    private final String senha;

    public PostgresConnectionFactory() {
        this(
                ApplicationEnvironment.getOrDefault("EVENTOS_DB_URL", "jdbc:postgresql://localhost:5432/eventos"),
                ApplicationEnvironment.getOrDefault("EVENTOS_DB_USER", "eventos"),
                ApplicationEnvironment.getOrDefault("EVENTOS_DB_PASSWORD", "eventos_dev")
        );
    }

    PostgresConnectionFactory(String url, String usuario, String senha) {
        if (url == null || !url.startsWith("jdbc:postgresql://")) {
            throw new IllegalArgumentException("EVENTOS_DB_URL deve ser uma URL JDBC PostgreSQL.");
        }
        this.url = url;
        this.usuario = usuario;
        this.senha = senha;
    }

    public Connection abrirConexao() throws SQLException {
        return DriverManager.getConnection(url, usuario, senha);
    }
}
