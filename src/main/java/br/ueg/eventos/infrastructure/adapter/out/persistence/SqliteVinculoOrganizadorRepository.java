package br.ueg.eventos.infrastructure.adapter.out.persistence;

import br.ueg.eventos.application.port.out.VinculoOrganizadorRepositoryPort;
import br.ueg.eventos.domain.model.VinculoOrganizador;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteVinculoOrganizadorRepository extends SqliteRepositorySupport
        implements VinculoOrganizadorRepositoryPort {

    public SqliteVinculoOrganizadorRepository(
            SqliteConnectionFactory connectionFactory
    ) {
        super(connectionFactory);
    }

    @Override
    public VinculoOrganizador salvar(VinculoOrganizador vinculo) {
        if (vinculo == null) {
            throw new IllegalArgumentException("O vínculo do organizador é obrigatório.");
        }

        String sql = vinculo.getId() == null
                ? """
                    INSERT INTO vinculos_organizadores (
                        usuario_id,
                        evento_id,
                        criador_principal
                    ) VALUES (?, ?, ?)
                    """
                : """
                    UPDATE vinculos_organizadores
                    SET usuario_id = ?,
                        evento_id = ?,
                        criador_principal = ?
                    WHERE id = ?
                    """;

        try (Connection connection = connectionFactory.abrirConexao();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            preencherParametros(statement, vinculo);
            if (vinculo.getId() != null) {
                statement.setLong(4, vinculo.getId());
                if (statement.executeUpdate() == 0) {
                    throw new IllegalArgumentException(
                            "Não existe vínculo de organizador com id " + vinculo.getId() + "."
                    );
                }
                return vinculo;
            }

            statement.executeUpdate();
            long id = ultimoIdInserido(connection);
            return new VinculoOrganizador(
                    id,
                    vinculo.getUsuarioId(),
                    vinculo.getEventoId(),
                    vinculo.isCriadorPrincipal()
            );
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Erro ao salvar o vínculo de organizador no SQLite.", e
            );
        }
    }

    @Override
    public Optional<VinculoOrganizador> buscarPorUsuarioEEvento(
            Integer usuarioId,
            Integer eventoId
    ) {
        if (usuarioId == null || eventoId == null) {
            return Optional.empty();
        }

        String sql = """
                SELECT id, usuario_id, evento_id, criador_principal
                FROM vinculos_organizadores
                WHERE usuario_id = ? AND evento_id = ?
                """;
        try (Connection connection = connectionFactory.abrirConexao();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, usuarioId);
            statement.setInt(2, eventoId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next()
                        ? Optional.of(mapear(resultSet))
                        : Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Erro ao buscar vínculo de organizador no SQLite.", e
            );
        }
    }

    @Override
    public List<VinculoOrganizador> listarPorEvento(Integer eventoId) {
        return listarPor("evento_id", eventoId);
    }

    @Override
    public List<VinculoOrganizador> listarPorUsuario(Integer usuarioId) {
        return listarPor("usuario_id", usuarioId);
    }

    @Override
    public void deletarPorEvento(Integer eventoId) {
        if (eventoId == null) {
            return;
        }

        try (Connection connection = connectionFactory.abrirConexao();
             PreparedStatement statement = connection.prepareStatement(
                     "DELETE FROM vinculos_organizadores WHERE evento_id = ?")) {
            statement.setInt(1, eventoId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Erro ao excluir vínculos de organizador no SQLite.", e
            );
        }
    }

    private List<VinculoOrganizador> listarPor(String coluna, Integer id) {
        if (id == null) {
            return List.of();
        }

        // coluna é definida somente pelos dois métodos acima, nunca por entrada HTTP.
        String sql = "SELECT id, usuario_id, evento_id, criador_principal "
                + "FROM vinculos_organizadores WHERE " + coluna + " = ? ORDER BY id";
        try (Connection connection = connectionFactory.abrirConexao();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<VinculoOrganizador> vinculos = new ArrayList<>();
                while (resultSet.next()) {
                    vinculos.add(mapear(resultSet));
                }
                return vinculos;
            }
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Erro ao listar vínculos de organizador no SQLite.", e
            );
        }
    }

    private void preencherParametros(
            PreparedStatement statement,
            VinculoOrganizador vinculo
    ) throws SQLException {
        statement.setInt(1, vinculo.getUsuarioId());
        statement.setInt(2, vinculo.getEventoId());
        statement.setInt(3, vinculo.isCriadorPrincipal() ? 1 : 0);
    }

    private VinculoOrganizador mapear(ResultSet resultSet) throws SQLException {
        return new VinculoOrganizador(
                resultSet.getLong("id"),
                resultSet.getInt("usuario_id"),
                resultSet.getInt("evento_id"),
                resultSet.getInt("criador_principal") == 1
        );
    }
}
