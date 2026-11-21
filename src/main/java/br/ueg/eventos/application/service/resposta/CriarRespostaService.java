package br.ueg.eventos.application.service.resposta;

import br.ueg.eventos.application.port.in.resposta.CriarRespostaPort;
import br.ueg.eventos.application.port.out.RespostaRepositoryPort;
import br.ueg.eventos.application.port.out.AtividadeRepositoryPort;
import br.ueg.eventos.application.port.out.FrequenciaRepositoryPort;
import br.ueg.eventos.application.port.out.InscricaoRepositoryPort;
import br.ueg.eventos.application.port.out.QuestaoRepositoryPort;
import br.ueg.eventos.application.port.out.QuestionarioRepositoryPort;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Atividade;
import br.ueg.eventos.domain.model.Frequencia;
import br.ueg.eventos.domain.model.Inscricao;
import br.ueg.eventos.domain.model.Questao;
import br.ueg.eventos.domain.model.Resposta;
import br.ueg.eventos.domain.model.TipoFrequenciaEnum;
import java.util.Comparator;
import java.util.List;

public class CriarRespostaService implements CriarRespostaPort {
    private final RespostaRepositoryPort respostaRepository;
    private final QuestaoRepositoryPort questoes;
    private final QuestionarioRepositoryPort questionarios;
    private final InscricaoRepositoryPort inscricoes;
    private final FrequenciaRepositoryPort frequencias;
    private final AtividadeRepositoryPort atividades;

    public CriarRespostaService(RespostaRepositoryPort respostaRepository,
                                QuestaoRepositoryPort questoes,
                                QuestionarioRepositoryPort questionarios,
                                InscricaoRepositoryPort inscricoes,
                                FrequenciaRepositoryPort frequencias,
                                AtividadeRepositoryPort atividades) {
        this.respostaRepository = respostaRepository;
        this.questoes = questoes;
        this.questionarios = questionarios;
        this.inscricoes = inscricoes;
        this.frequencias = frequencias;
        this.atividades = atividades;
    }

    @Override
    public Resposta executar(ComandoCriarResposta comando) {
        if (comando == null || comando.idQuestao == null || comando.idUsuario == null
                || comando.idUsuario <= 0 || comando.valor == null || comando.valor.isBlank()) {
            throw new RegraNegocioException("Questão, usuário autenticado e resposta são obrigatórios.");
        }
        Questao questao = questoes.buscarPorId(comando.idQuestao)
                .orElseThrow(() -> new RegraNegocioException("Questão não encontrada."));
        var questionario = questionarios.buscarPorId(questao.getIdQuestionario().longValue())
                .orElseThrow(() -> new RegraNegocioException("Questionário não encontrado."));
        if (!questionario.isPublicado()) throw new RegraNegocioException("Este questionário ainda não está publicado.");
        if (respostaRepository.buscarPorIdQuestao(comando.idQuestao).stream()
                .anyMatch(resposta -> resposta.getIdUsuario().equals(comando.idUsuario))) {
            throw new RegraNegocioException("Você já respondeu esta questão.");
        }

        Atividade atividade = atividades.buscarPorId(questionario.getIdAtividade().longValue())
                .orElseThrow(() -> new RegraNegocioException("Atividade da avaliação não encontrada."));
        Inscricao inscricao = inscricoes.listarPorAtividade(atividade.getId()).stream()
                .filter(item -> item.estaAtiva() && item.getIdUsuario().equals(comando.idUsuario.longValue()))
                .findFirst()
                .orElseThrow(() -> new RegraNegocioException("A avaliação está disponível somente para participantes inscritos na atividade."));
        if (!presencaValidada(inscricao, atividade)) {
            throw new RegraNegocioException("A avaliação está disponível somente após a presença ser validada.");
        }
        validarValor(questao, comando.valor.trim());
        Integer idQuestao = comando.idQuestao != null ? comando.idQuestao.intValue() : null;
        Resposta resposta = new Resposta(idQuestao, comando.idUsuario, comando.valor);
        return respostaRepository.salvar(resposta);
    }

    private boolean presencaValidada(Inscricao inscricao, Atividade atividade) {
        List<Frequencia> marcacoes = frequencias.listarPorInscricao(Math.toIntExact(inscricao.getId()));
        TipoFrequenciaEnum politica = atividade.getTipoFrequencia();
        if (politica == TipoFrequenciaEnum.CHECKIN) {
            return marcacoes.stream().anyMatch(f -> f.isPresente() && "CHECKIN".equalsIgnoreCase(f.getTipo()));
        }
        if (politica == TipoFrequenciaEnum.ENTRADA_SAIDA) {
            boolean entrada = marcacoes.stream().anyMatch(f -> f.isPresente() && "QR_ENTRADA".equalsIgnoreCase(f.getOrigem()));
            boolean saida = marcacoes.stream().anyMatch(f -> f.isPresente() && "QR_SAIDA".equalsIgnoreCase(f.getOrigem()));
            return entrada && saida;
        }
        return marcacoes.stream().filter(f -> "MANUAL".equalsIgnoreCase(f.getTipo()))
                .max(Comparator.comparing(Frequencia::getDataHora)).map(Frequencia::isPresente).orElse(false);
    }

    private void validarValor(Questao questao, String valor) {
        if (questao.getTipo() == Questao.TipoQuestao.ESCOLHA_UNICA
                && !questao.getOpcoes().contains(valor)) {
            throw new RegraNegocioException("Selecione uma das opções cadastradas para esta questão.");
        }
        if (questao.getTipo() == Questao.TipoQuestao.ESCALA_NUMERICA) {
            try {
                int numero = Integer.parseInt(valor);
                if (numero < questao.getEscalaMinima() || numero > questao.getEscalaMaxima()) {
                    throw new RegraNegocioException("A resposta deve ficar entre " + questao.getEscalaMinima()
                            + " e " + questao.getEscalaMaxima() + ".");
                }
            } catch (NumberFormatException e) {
                throw new RegraNegocioException("A resposta desta questão deve ser numérica.");
            }
        }
    }
}
