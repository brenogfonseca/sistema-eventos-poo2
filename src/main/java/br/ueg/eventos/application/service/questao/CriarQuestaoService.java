package br.ueg.eventos.application.service.questao;

import br.ueg.eventos.application.port.in.questao.CriarQuestaoPort;
import br.ueg.eventos.application.port.out.QuestaoRepository;
import br.ueg.eventos.domain.model.Questao;

public class CriarQuestaoService implements CriarQuestaoPort {

    private final QuestaoRepository questaoRepository;

    public CriarQuestaoService(QuestaoRepository questaoRepository) {
        this.questaoRepository = questaoRepository;
    }

    @Override
    public Questao executar(ComandoCriarQuestao comando) {
        Questao questao = new Questao(comando.enunciado, comando.tipo, comando.idQuestionario);
        return questaoRepository.salvar(questao);
    }
}