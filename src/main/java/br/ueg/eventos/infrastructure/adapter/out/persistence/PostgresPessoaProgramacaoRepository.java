package br.ueg.eventos.infrastructure.adapter.out.persistence;

import br.ueg.eventos.application.port.out.PessoaProgramacaoRepositoryPort;
import br.ueg.eventos.domain.model.PessoaProgramacao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class PostgresPessoaProgramacaoRepository implements PessoaProgramacaoRepositoryPort {
    private final PostgresConnectionFactory connections;

    public PostgresPessoaProgramacaoRepository(PostgresConnectionFactory connections) {
        this.connections = connections;
    }

    @Override
    public PessoaProgramacao vincular(Long idAtividade, PessoaProgramacao pessoa, String papel) {
        String inserirPessoa = "INSERT INTO pessoas_programacao(nome, organizacao, biografia, foto_url) VALUES (?, ?, ?, ?) RETURNING id";
        try (Connection connection = connections.abrirConexao()) {
            connection.setAutoCommit(false);
            Long idPessoa;
            try {
                try (PreparedStatement statement = connection.prepareStatement(inserirPessoa)) {
                    statement.setString(1, pessoa.getNome());
                    statement.setString(2, pessoa.getOrganizacao());
                    statement.setString(3, pessoa.getBiografia());
                    statement.setString(4, pessoa.getFotoUrl());
                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (!resultSet.next()) throw new SQLException("Pessoa da programação não foi criada.");
                        idPessoa = resultSet.getLong(1);
                    }
                }
                try (PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO atividade_pessoas(atividade_id, pessoa_id, papel) VALUES (?, ?, ?)")) {
                    statement.setLong(1, idAtividade);
                    statement.setLong(2, idPessoa);
                    statement.setString(3, papel);
                    statement.executeUpdate();
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
            return new PessoaProgramacao(idPessoa, pessoa.getNome(), pessoa.getOrganizacao(), pessoa.getBiografia(), pessoa.getFotoUrl());
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao vincular pessoa à atividade no PostgreSQL.", e);
        }
    }

    @Override
    public List<VinculoPessoa> listarPorAtividade(Long idAtividade) {
        String sql = """
                SELECT p.id, p.nome, p.organizacao, p.biografia, p.foto_url, ap.papel
                FROM atividade_pessoas ap
                JOIN pessoas_programacao p ON p.id = ap.pessoa_id
                WHERE ap.atividade_id = ?
                ORDER BY p.nome, ap.papel
                """;
        try (Connection connection = connections.abrirConexao();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, idAtividade);
            try (ResultSet rs = statement.executeQuery()) {
                List<VinculoPessoa> resultado = new ArrayList<>();
                while (rs.next()) {
                    PessoaProgramacao pessoa = new PessoaProgramacao(rs.getLong("id"), rs.getString("nome"),
                            rs.getString("organizacao"), rs.getString("biografia"), rs.getString("foto_url"));
                    resultado.add(new VinculoPessoa(idAtividade, rs.getString("papel"), pessoa));
                }
                return resultado;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao listar participantes da programação.", e);
        }
    }

    @Override
    public List<VinculoPessoa> listarPorEvento(Long idEvento) {
        String sql = """
                SELECT ap.atividade_id, p.id, p.nome, p.organizacao, p.biografia, p.foto_url, ap.papel
                FROM atividade_pessoas ap
                JOIN atividades a ON a.id = ap.atividade_id
                JOIN pessoas_programacao p ON p.id = ap.pessoa_id
                WHERE a.evento_id = ?
                ORDER BY a.data, a.hora_inicio, a.id, p.nome, ap.papel
                """;
        try (Connection connection = connections.abrirConexao();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, idEvento);
            try (ResultSet rs = statement.executeQuery()) {
                List<VinculoPessoa> resultado = new ArrayList<>();
                while (rs.next()) {
                    long idAtividade = rs.getLong("atividade_id");
                    PessoaProgramacao pessoa = new PessoaProgramacao(rs.getLong("id"), rs.getString("nome"),
                            rs.getString("organizacao"), rs.getString("biografia"), rs.getString("foto_url"));
                    resultado.add(new VinculoPessoa(idAtividade, rs.getString("papel"), pessoa));
                }
                return resultado;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao listar pessoas da programação do evento.", e);
        }
    }
}
