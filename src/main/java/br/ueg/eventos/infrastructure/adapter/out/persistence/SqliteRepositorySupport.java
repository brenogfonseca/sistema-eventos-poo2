package br.ueg.eventos.infrastructure.adapter.out.persistence;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

abstract class SqliteRepositorySupport {

    protected final SqliteConnectionFactory connectionFactory;

    protected SqliteRepositorySupport(
            SqliteConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    protected long ultimoIdInserido(Connection connection)
            throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet =
                     statement.executeQuery("SELECT last_insert_rowid()")) {
            if (!resultSet.next()) {
                throw new SQLException(
                        "Não foi possível obter o id do registro inserido."
                );
            }
            return resultSet.getLong(1);
        }
    }
}
