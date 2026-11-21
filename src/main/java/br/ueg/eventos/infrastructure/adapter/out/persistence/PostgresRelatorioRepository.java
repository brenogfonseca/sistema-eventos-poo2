package br.ueg.eventos.infrastructure.adapter.out.persistence;

import br.ueg.eventos.application.port.out.RelatorioRepositoryPort;
import br.ueg.eventos.domain.model.TipoFrequenciaEnum;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class PostgresRelatorioRepository implements RelatorioRepositoryPort {
    private final PostgresConnectionFactory connections;

    public PostgresRelatorioRepository(PostgresConnectionFactory connections) {
        this.connections = connections;
    }

    @Override
    public String exportarInscricoesCsv(Long idEvento) {
        String sql = """
                SELECT e.titulo AS evento, ie.id AS inscricao_id, u.nome, u.email,
                       CASE WHEN ie.cancelada = 1 THEN 'CANCELADA' ELSE 'ATIVA' END AS status,
                       COUNT(i.id) FILTER (WHERE i.cancelada = 0) AS atividades_ativas
                FROM inscricoes_evento ie
                JOIN eventos e ON e.id = ie.evento_id
                JOIN usuarios u ON u.id = ie.usuario_id
                LEFT JOIN inscricoes i ON i.inscricao_evento_id = ie.id
                WHERE e.id = ?
                GROUP BY e.titulo, ie.id, u.nome, u.email, ie.cancelada
                ORDER BY u.nome, ie.id
                """;
        StringBuilder csv = new StringBuilder("\uFEFFevento,inscricao_id,nome,email,status,atividades_ativas\r\n");
        try (Connection connection = connections.abrirConexao();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, idEvento);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    linha(csv, rs.getString("evento"), rs.getLong("inscricao_id"), rs.getString("nome"),
                            rs.getString("email"), rs.getString("status"), rs.getLong("atividades_ativas"));
                }
            }
            return csv.toString();
        } catch (SQLException e) {
            throw new IllegalStateException("Falha ao gerar relatório de inscrições.", e);
        }
    }

    @Override
    public String exportarFrequenciasCsv(Long idEvento) {
        String sql = """
                SELECT ie.id AS inscricao_evento_id, ie.cancelada AS evento_cancelado,
                       i.id AS inscricao_id, i.cancelada AS atividade_cancelada,
                       e.titulo AS evento, a.nome AS atividade, a.tipo_frequencia,
                       u.id AS usuario_id, u.nome, u.email,
                       f.id AS frequencia_id, f.data_hora, f.origem, f.presente, f.tipo
                FROM inscricoes_evento ie
                JOIN eventos e ON e.id = ie.evento_id
                JOIN usuarios u ON u.id = ie.usuario_id
                JOIN inscricoes i ON i.inscricao_evento_id = ie.id
                JOIN atividades a ON a.id = i.atividade_id
                LEFT JOIN frequencias f ON f.inscricao_id = i.id
                WHERE e.id = ?
                ORDER BY a.data, a.hora_inicio, u.nome, f.data_hora, f.id
                """;
        Map<String, GrupoFrequencia> grupos = new LinkedHashMap<>();
        try (Connection connection = connections.abrirConexao();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, idEvento);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    long idInscricao = rs.getLong("inscricao_id");
                    String chave = idInscricao + ":" + rs.getLong("usuario_id");
                    GrupoFrequencia grupo = grupos.computeIfAbsent(chave, ignored -> criarGrupo(rs, idInscricao));
                    int idFrequencia = rs.getInt("frequencia_id");
                    if (!rs.wasNull()) {
                        grupo.marcacoes.add(new Marca(LocalDateTime.parse(rs.getString("data_hora")),
                                rs.getString("origem"), rs.getInt("presente") == 1, rs.getString("tipo")));
                    }
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Falha ao gerar relatório de frequência.", e);
        }
        StringBuilder csv = new StringBuilder("\uFEFFevento,atividade,inscricao_evento_id,inscricao_atividade_id,usuario_id,nome,email,status_inscricao,criterio,presenca_validada,registros_frequencia\r\n");
        for (GrupoFrequencia g : grupos.values()) {
            boolean validada = validar(g.criterio, g.marcacoes);
            linha(csv, g.evento, g.atividade, g.idInscricaoEvento, g.idInscricao,
                    g.idUsuario, g.nome, g.email,
                    g.cancelada ? "CANCELADA" : "ATIVA", g.criterio,
                    validada ? "SIM" : "NAO", g.marcacoes.size());
        }
        return csv.toString();
    }

    private GrupoFrequencia criarGrupo(ResultSet rs, long idInscricao) {
        try {
            return new GrupoFrequencia(rs.getLong("inscricao_evento_id"), idInscricao,
                    rs.getLong("usuario_id"), rs.getString("evento"), rs.getString("atividade"),
                    rs.getString("nome"), rs.getString("email"),
                    rs.getInt("atividade_cancelada") == 1 || rs.getInt("evento_cancelado") == 1,
                    rs.getString("tipo_frequencia"), new ArrayList<>());
        } catch (SQLException e) {
            throw new IllegalStateException("Não foi possível ler uma linha do relatório.", e);
        }
    }

    private boolean validar(String politicaTexto, List<Marca> marcacoes) {
        TipoFrequenciaEnum politica;
        try { politica = TipoFrequenciaEnum.valueOf(politicaTexto); }
        catch (Exception e) { return false; }
        if (politica == TipoFrequenciaEnum.CHECKIN) {
            return marcacoes.stream().anyMatch(f -> f.presente && "CHECKIN".equalsIgnoreCase(f.tipo));
        }
        if (politica == TipoFrequenciaEnum.ENTRADA_SAIDA) {
            boolean entrada = marcacoes.stream().anyMatch(f -> f.presente && "QR_ENTRADA".equalsIgnoreCase(f.origem));
            boolean saida = marcacoes.stream().anyMatch(f -> f.presente && "QR_SAIDA".equalsIgnoreCase(f.origem));
            return entrada && saida;
        }
        return marcacoes.stream().filter(f -> "MANUAL".equalsIgnoreCase(f.tipo))
                .max(java.util.Comparator.comparing(Marca::dataHora))
                .map(Marca::presente).orElse(false);
    }

    private void linha(StringBuilder csv, Object... valores) {
        for (int i = 0; i < valores.length; i++) {
            if (i > 0) csv.append(',');
            String valor = valores[i] == null ? "" : String.valueOf(valores[i]);
            if (!valor.isEmpty() && "=+-@".indexOf(valor.charAt(0)) >= 0) valor = "'" + valor;
            csv.append('"').append(valor.replace("\"", "\"\"")).append('"');
        }
        csv.append("\r\n");
    }

    private record GrupoFrequencia(long idInscricaoEvento, long idInscricao, long idUsuario,
                                   String evento, String atividade, String nome, String email,
                                   boolean cancelada, String criterio, List<Marca> marcacoes) { }
    private record Marca(LocalDateTime dataHora, String origem, boolean presente, String tipo) { }
}
