package br.ueg.eventos.infrastructure.adapter.out.persistence;

import br.ueg.eventos.application.port.out.VinculoColaboradorRepositoryPort;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class PostgresVinculoColaboradorRepository extends PostgresRepositorySupport
        implements VinculoColaboradorRepositoryPort {
    public PostgresVinculoColaboradorRepository(PostgresConnectionFactory factory) {
        super(factory);
    }

    @Override
    public void vincular(Integer idUsuario, Long idAtividade) {
        executar("INSERT INTO vinculos_colaboradores(usuario_id, atividade_id) VALUES (?, ?) ON CONFLICT DO NOTHING",
                idUsuario, idAtividade);
    }

    @Override
    public void remover(Integer idUsuario, Long idAtividade) {
        executar("DELETE FROM vinculos_colaboradores WHERE usuario_id = ? AND atividade_id = ?",
                idUsuario, idAtividade);
    }

    @Override
    public boolean estaVinculado(Integer idUsuario, Long idAtividade) {
        String sql = "SELECT 1 FROM vinculos_colaboradores WHERE usuario_id = ? AND atividade_id = ?";
        try (Connection connection = connectionFactory.abrirConexao();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idUsuario);
            statement.setLong(2, idAtividade);
            try (ResultSet rs = statement.executeQuery()) { return rs.next(); }
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao consultar permissão do colaborador.", e);
        }
    }

    @Override
    public List<Integer> listarUsuariosPorAtividade(Long idAtividade) {
        return listar("SELECT usuario_id FROM vinculos_colaboradores WHERE atividade_id = ? ORDER BY usuario_id",
                idAtividade).stream().map(Number::intValue).toList();
    }

    @Override
    public List<Long> listarAtividadesPorUsuario(Integer idUsuario) {
        return listar("SELECT atividade_id FROM vinculos_colaboradores WHERE usuario_id = ? ORDER BY atividade_id",
                idUsuario).stream().map(Number::longValue).toList();
    }

    private void executar(String sql, Integer idUsuario, Long idAtividade) {
        try (Connection connection = connectionFactory.abrirConexao();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idUsuario);
            statement.setLong(2, idAtividade);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao atualizar vínculo de colaborador.", e);
        }
    }

    private List<Number> listar(String sql, Number id) {
        try (Connection connection = connectionFactory.abrirConexao();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id.longValue());
            try (ResultSet rs = statement.executeQuery()) {
                List<Number> valores = new ArrayList<>();
                while (rs.next()) valores.add(rs.getLong(1));
                return valores;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao listar vínculos de colaboradores.", e);
        }
    }
}
