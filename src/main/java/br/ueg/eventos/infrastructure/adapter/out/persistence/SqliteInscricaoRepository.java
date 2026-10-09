package br.ueg.eventos.infrastructure.adapter.out.persistence;

import br.ueg.eventos.application.port.out.InscricaoRepositoryPort;
import br.ueg.eventos.domain.model.Inscricao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteInscricaoRepository extends SqliteRepositorySupport
        implements InscricaoRepositoryPort {

    private static final String INSERT = """
            INSERT INTO inscricoes (atividade_id, usuario_id, cancelada)
            VALUES (?, ?, ?)
            """;

    private static final String UPDATE = """
            UPDATE inscricoes
            SET atividade_id = ?, usuario_id = ?, cancelada = ?
            WHERE id = ?
            """;

    public SqliteInscricaoRepository(
            SqliteConnectionFactory connectionFactory) {
        super(connectionFactory);
    }

    @Override
    public Inscricao salvar(Inscricao inscricao) {
        if (inscricao == null) {
            throw new IllegalArgumentException(
                    "A inscrição não pode ser nula."
            );
        }

        try (Connection connection = connectionFactory.abrirConexao()) {
            if (inscricao.getId() == null) {
                try (PreparedStatement statement =
                             connection.prepareStatement(INSERT)) {
                    preencherParametros(statement, inscricao);
                    statement.executeUpdate();
                }
                long id = ultimoIdInserido(connection);
                return buscarPorId(connection, id)
                        .orElseThrow(() -> new SQLException(
                                "A inscrição inserida não foi encontrada."
                        ));
            }

            try (PreparedStatement statement =
                         connection.prepareStatement(UPDATE)) {
                preencherParametros(statement, inscricao);
                statement.setLong(4, inscricao.getId());
                if (statement.executeUpdate() == 0) {
                    throw new SQLException(
                            "Não existe inscrição com id " + inscricao.getId()
                    );
                }
            }
            return buscarPorId(connection, inscricao.getId())
                    .orElseThrow(() -> new SQLException(
                            "A inscrição atualizada não foi encontrada."
                    ));
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Erro ao salvar inscrição no SQLite.", e
            );
        }
    }

    @Override
    public Optional<Inscricao> buscarPorId(Long id) {
        if (id == null || id <= 0) {
            return Optional.empty();
        }
        try (Connection connection = connectionFactory.abrirConexao()) {
            return buscarPorId(connection, id);
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Erro ao buscar inscrição no SQLite.", e
            );
        }
    }

    @Override
    public List<Inscricao> listarPorAtividade(Long idAtividade) {
        return listarPorCampo("atividade_id", idAtividade);
    }

    @Override
    public List<Inscricao> listarPorUsuario(Long idUsuario) {
        return listarPorCampo("usuario_id", idUsuario);
    }

    @Override
    public void excluir(Long id) {
        if (id == null || id <= 0) {
            return;
        }
        try (Connection connection = connectionFactory.abrirConexao();
             PreparedStatement statement = connection.prepareStatement(
                     "DELETE FROM inscricoes WHERE id = ?")) {
            statement.setLong(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Erro ao excluir inscrição no SQLite.", e
            );
        }
    }

    private List<Inscricao> listarPorCampo(String campo, Long valor) {
        if (valor == null || valor <= 0) {
            return List.of();
        }
        String sql = "SELECT * FROM inscricoes WHERE " + campo + " = ? "
                + "ORDER BY id";
        try (Connection connection = connectionFactory.abrirConexao();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, valor);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Inscricao> inscricoes = new ArrayList<>();
                while (resultSet.next()) {
                    inscricoes.add(mapearInscricao(resultSet));
                }
                return inscricoes;
            }
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Erro ao listar inscrições no SQLite.", e
            );
        }
    }

    private Optional<Inscricao> buscarPorId(
            Connection connection,
            Long id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM inscricoes WHERE id = ?")) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next()
                        ? Optional.of(mapearInscricao(resultSet))
                        : Optional.empty();
            }
        }
    }

    private void preencherParametros(
            PreparedStatement statement,
            Inscricao inscricao) throws SQLException {
        statement.setLong(1, inscricao.getIdAtividade());
        statement.setLong(2, inscricao.getIdUsuario());
        statement.setInt(3, inscricao.isCancelada() ? 1 : 0);
    }

    private Inscricao mapearInscricao(ResultSet resultSet)
            throws SQLException {
        return new Inscricao(
                resultSet.getLong("id"),
                resultSet.getLong("atividade_id"),
                resultSet.getLong("usuario_id"),
                resultSet.getInt("cancelada") == 1
        );
    }
}
