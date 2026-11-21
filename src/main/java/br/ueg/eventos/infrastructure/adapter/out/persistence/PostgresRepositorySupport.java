package br.ueg.eventos.infrastructure.adapter.out.persistence;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

abstract class PostgresRepositorySupport {

    protected final PostgresConnectionFactory connectionFactory;

    protected PostgresRepositorySupport(
            PostgresConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    protected long ultimoIdInserido(Connection connection)
            throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet =
                     statement.executeQuery("SELECT lastval()")) {
            if (!resultSet.next()) {
                throw new SQLException(
                        "Não foi possível obter o id do registro inserido."
                );
            }
            return resultSet.getLong(1);
        }
    }
}
