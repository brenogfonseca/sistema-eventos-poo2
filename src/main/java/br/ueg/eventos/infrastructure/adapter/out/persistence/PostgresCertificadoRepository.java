package br.ueg.eventos.infrastructure.adapter.out.persistence;

import br.ueg.eventos.application.port.out.CertificadoRepositoryPort;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public final class PostgresCertificadoRepository implements CertificadoRepositoryPort {
    private final PostgresConnectionFactory connections;

    public PostgresCertificadoRepository(PostgresConnectionFactory connections) {
        this.connections = connections;
    }

    @Override
    public void registrar(Long idEvento, Long idUsuario, int cargaHorariaMinutos, String arquivoNome) {
        String sql = """
                INSERT INTO certificados (evento_id, usuario_id, tipo, carga_horaria_minutos, arquivo_nome)
                VALUES (?, ?, 'PARTICIPANTE', ?, ?)
                ON CONFLICT (evento_id, usuario_id, tipo)
                    WHERE usuario_id IS NOT NULL
                DO UPDATE SET carga_horaria_minutos = EXCLUDED.carga_horaria_minutos,
                              arquivo_nome = EXCLUDED.arquivo_nome
                """;
        try (Connection connection = connections.abrirConexao();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, idEvento);
            statement.setLong(2, idUsuario);
            statement.setInt(3, cargaHorariaMinutos);
            statement.setString(4, arquivoNome);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Não foi possível registrar a emissão do certificado.", e);
        }
    }

    @Override
    public void registrarPessoaProgramacao(Long idEvento, Long idPessoa, String papel,
                                           int cargaHorariaMinutos, String arquivoNome) {
        String sql = """
                INSERT INTO certificados (
                    evento_id, usuario_id, pessoa_programacao_id, tipo,
                    carga_horaria_minutos, arquivo_nome
                ) VALUES (?, NULL, ?, ?, ?, ?)
                ON CONFLICT (evento_id, pessoa_programacao_id, tipo)
                    WHERE pessoa_programacao_id IS NOT NULL
                DO UPDATE SET carga_horaria_minutos = EXCLUDED.carga_horaria_minutos,
                              arquivo_nome = EXCLUDED.arquivo_nome
                """;
        try (Connection connection = connections.abrirConexao();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, idEvento);
            statement.setLong(2, idPessoa);
            statement.setString(3, papel);
            statement.setInt(4, cargaHorariaMinutos);
            statement.setString(5, arquivoNome);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Não foi possível registrar o certificado da programação.", e);
        }
    }
}
