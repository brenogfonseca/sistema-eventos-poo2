package br.ueg.eventos.application.service.questao;

import br.ueg.eventos.application.port.in.questao.CriarQuestaoPort;
import br.ueg.eventos.application.port.out.QuestaoRepositoryPort;
import br.ueg.eventos.domain.model.Questao;

public class CriarQuestaoService implements CriarQuestaoPort {

    private final QuestaoRepositoryPort questaoRepository;

    public CriarQuestaoService(QuestaoRepositoryPort questaoRepository) {
        this.questaoRepository = questaoRepository;
    }

    @Override
    public Questao executar(ComandoCriarQuestao comando) {
        Integer idQuestionario = comando.idQuestionario != null ? comando.idQuestionario.intValue() : null;
        Questao questao = new Questao(comando.enunciado, comando.tipo, idQuestionario);
        return questaoRepository.salvar(questao);
    }
}
