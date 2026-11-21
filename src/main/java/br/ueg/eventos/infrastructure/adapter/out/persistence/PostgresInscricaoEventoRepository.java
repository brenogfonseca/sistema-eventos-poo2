package br.ueg.eventos.infrastructure.adapter.out.persistence;

import br.ueg.eventos.application.port.out.InscricaoEventoRepositoryPort;
import br.ueg.eventos.domain.model.InscricaoEvento;
import br.ueg.eventos.domain.exception.RegraNegocioException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PostgresInscricaoEventoRepository extends PostgresRepositorySupport
        implements InscricaoEventoRepositoryPort {

    public PostgresInscricaoEventoRepository(PostgresConnectionFactory factory) {
        super(factory);
    }

    @Override
    public InscricaoEvento salvar(InscricaoEvento inscricao) {
        String sql = inscricao.getId() == null
                ? "INSERT INTO inscricoes_evento(evento_id, usuario_id, cancelada) VALUES (?, ?, ?)"
                : "UPDATE inscricoes_evento SET evento_id=?, usuario_id=?, cancelada=? WHERE id=?";
        try (Connection connection = connectionFactory.abrirConexao();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, inscricao.getIdEvento());
            statement.setLong(2, inscricao.getIdUsuario());
            statement.setInt(3, inscricao.isCancelada() ? 1 : 0);
            if (inscricao.getId() != null) {
                statement.setLong(4, inscricao.getId());
                if (statement.executeUpdate() == 0) throw new SQLException("Inscrição no evento não encontrada.");
                return inscricao;
            }
            statement.executeUpdate();
            return buscarPorId(connection, ultimoIdInserido(connection))
                    .orElseThrow(() -> new SQLException("Inscrição no evento não encontrada após salvar."));
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao salvar inscrição no evento no PostgreSQL.", e);
        }
    }

    @Override
    public Optional<InscricaoEvento> buscarPorId(Long id) {
        if (id == null || id <= 0) return Optional.empty();
        try (Connection connection = connectionFactory.abrirConexao()) {
            return buscarPorId(connection, id);
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao buscar inscrição no evento no PostgreSQL.", e);
        }
    }

    @Override
    public Optional<InscricaoEvento> buscarPorEventoEUsuario(Long idEvento, Long idUsuario) {
        if (idEvento == null || idUsuario == null) return Optional.empty();
        String sql = "SELECT * FROM inscricoes_evento WHERE evento_id=? AND usuario_id=? ORDER BY id DESC LIMIT 1";
        try (Connection connection = connectionFactory.abrirConexao();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, idEvento);
            statement.setLong(2, idUsuario);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao consultar inscrição no evento no PostgreSQL.", e);
        }
    }

    @Override
    public List<InscricaoEvento> listarPorEvento(Long idEvento) {
        return listar("evento_id", idEvento);
    }

    @Override
    public List<InscricaoEvento> listarPorUsuario(Long idUsuario) {
        return listar("usuario_id", idUsuario);
    }

    @Override
    public InscricaoEvento inscreverSeDisponivel(Long idEvento, Long idUsuario, int capacidadeInformada) {
        String eventoSql = "SELECT capacidade_maxima FROM eventos WHERE id = ? FOR UPDATE";
        try (Connection connection = connectionFactory.abrirConexao()) {
            connection.setAutoCommit(false);
            try {
                int capacidade;
                try (PreparedStatement statement = connection.prepareStatement(eventoSql)) {
                    statement.setLong(1, idEvento);
                    try (ResultSet rs = statement.executeQuery()) {
                        if (!rs.next()) throw new RegraNegocioException("Evento não encontrado.");
                        capacidade = rs.getInt(1);
                    }
                }
                if (capacidade != capacidadeInformada) {
                    throw new RegraNegocioException("A capacidade do evento foi alterada. Atualize e tente novamente.");
                }
                InscricaoEvento existente = null;
                try (PreparedStatement statement = connection.prepareStatement(
                        "SELECT * FROM inscricoes_evento WHERE evento_id = ? AND usuario_id = ? FOR UPDATE")) {
                    statement.setLong(1, idEvento);
                    statement.setLong(2, idUsuario);
                    try (ResultSet rs = statement.executeQuery()) {
                        if (rs.next()) existente = mapear(rs);
                    }
                }
                if (existente != null && existente.estaAtiva()) {
                    connection.commit();
                    return existente;
                }
                long ativos;
                try (PreparedStatement statement = connection.prepareStatement(
                        "SELECT COUNT(*) FROM inscricoes_evento WHERE evento_id = ? AND cancelada = 0")) {
                    statement.setLong(1, idEvento);
                    try (ResultSet rs = statement.executeQuery()) {
                        rs.next();
                        ativos = rs.getLong(1);
                    }
                }
                if (ativos >= capacidade) throw new RegraNegocioException("Não há vagas disponíveis neste evento.");
                InscricaoEvento salva;
                if (existente != null) {
                    try (PreparedStatement statement = connection.prepareStatement(
                            "UPDATE inscricoes_evento SET cancelada = 0 WHERE id = ? RETURNING *")) {
                        statement.setLong(1, existente.getId());
                        try (ResultSet rs = statement.executeQuery()) {
                            if (!rs.next()) throw new SQLException("Falha ao reativar inscrição.");
                            salva = mapear(rs);
                        }
                    }
                } else {
                    try (PreparedStatement statement = connection.prepareStatement(
                            "INSERT INTO inscricoes_evento(evento_id, usuario_id, cancelada) VALUES (?, ?, 0) RETURNING *")) {
                        statement.setLong(1, idEvento);
                        statement.setLong(2, idUsuario);
                        try (ResultSet rs = statement.executeQuery()) {
                            if (!rs.next()) throw new SQLException("Falha ao criar inscrição.");
                            salva = mapear(rs);
                        }
                    }
                }
                connection.commit();
                return salva;
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao reservar vaga no evento.", e);
        }
    }

    private List<InscricaoEvento> listar(String coluna, Long id) {
        if (id == null || id <= 0) return List.of();
        String sql = "SELECT * FROM inscricoes_evento WHERE " + coluna + "=? ORDER BY id";
        try (Connection connection = connectionFactory.abrirConexao();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                List<InscricaoEvento> itens = new ArrayList<>();
                while (rs.next()) itens.add(mapear(rs));
                return itens;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao listar inscrições no evento no PostgreSQL.", e);
        }
    }

    private Optional<InscricaoEvento> buscarPorId(Connection c, Long id) throws SQLException {
        try (PreparedStatement statement = c.prepareStatement("SELECT * FROM inscricoes_evento WHERE id=?")) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    private InscricaoEvento mapear(ResultSet rs) throws SQLException {
        return new InscricaoEvento(rs.getLong("id"), rs.getLong("evento_id"),
                rs.getLong("usuario_id"), rs.getInt("cancelada") == 1);
    }
}
