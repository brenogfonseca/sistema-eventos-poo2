package br.ueg.eventos.application.service.questao;

import br.ueg.eventos.application.port.in.questao.RemoverQuestaoPort;
import br.ueg.eventos.application.port.out.QuestaoRepositoryPort;

public class RemoverQuestaoService implements RemoverQuestaoPort {

    private final QuestaoRepositoryPort questaoRepository;

    public RemoverQuestaoService(QuestaoRepositoryPort questaoRepository) {
        this.questaoRepository = questaoRepository;
    }

    @Override
    public void executar(Long id) {
        questaoRepository.excluir(id);
    }
}
