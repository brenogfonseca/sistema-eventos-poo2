package br.ueg.eventos.application.service.questionario;

import br.ueg.eventos.application.port.in.questionario.ExportarResultadosQuestionarioPort;
import br.ueg.eventos.application.port.out.QuestionarioRepositoryPort;
import br.ueg.eventos.application.port.out.QuestaoRepositoryPort;
import br.ueg.eventos.application.port.out.RespostaRepositoryPort;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Questao;

import java.util.Map;
import java.util.TreeMap;

public final class ExportarResultadosQuestionarioService implements ExportarResultadosQuestionarioPort {
    private final QuestionarioRepositoryPort questionarios;
    private final QuestaoRepositoryPort questoes;
    private final RespostaRepositoryPort respostas;

    public ExportarResultadosQuestionarioService(QuestionarioRepositoryPort questionarios,
                                                 QuestaoRepositoryPort questoes,
                                                 RespostaRepositoryPort respostas) {
        this.questionarios = questionarios;
        this.questoes = questoes;
        this.respostas = respostas;
    }

    @Override
    public String executar(Long idQuestionario) {
        var questionario = questionarios.buscarPorId(idQuestionario)
                .orElseThrow(() -> new RegraNegocioException("Questionário não encontrado."));
        StringBuilder csv = new StringBuilder("\uFEFFquestionario,questao_id,questao,tipo,resposta,quantidade\r\n");
        for (Questao questao : questoes.buscarPorIdQuestionario(idQuestionario)) {
            Map<String, Long> contagens = new TreeMap<>();
            respostas.buscarPorIdQuestao(questao.getId().longValue())
                    .forEach(resposta -> contagens.merge(resposta.getValor(), 1L, Long::sum));
            for (var entrada : contagens.entrySet()) {
                linha(csv, questionario.getTitulo(), questao.getId(), questao.getEnunciado(),
                        questao.getTipo().name(), entrada.getKey(), entrada.getValue());
            }
            if (questao.getTipo() == Questao.TipoQuestao.ESCALA_NUMERICA && !contagens.isEmpty()) {
                double media = respostas.buscarPorIdQuestao(questao.getId().longValue()).stream()
                        .mapToInt(r -> Integer.parseInt(r.getValor())).average().orElse(0);
                linha(csv, questionario.getTitulo(), questao.getId(), questao.getEnunciado(),
                        questao.getTipo().name(), "MÉDIA", String.format(java.util.Locale.ROOT, "%.2f", media));
            }
        }
        return csv.toString();
    }

    private void linha(StringBuilder csv, Object... valores) {
        for (int i = 0; i < valores.length; i++) {
            if (i > 0) csv.append(',');
            String valor = valores[i] == null ? "" : String.valueOf(valores[i]);
            csv.append('"').append(valor.replace("\"", "\"\"")).append('"');
        }
        csv.append("\r\n");
    }
}
