package br.ueg.eventos.application.service.questao;

import br.ueg.eventos.application.port.in.questao.BuscarQuestaoNoQuestionarioPort;
import br.ueg.eventos.application.port.out.QuestaoRepositoryPort;
import br.ueg.eventos.domain.model.Questao;
import java.util.List;
public class BuscarQuestaoNoQuestionarioService implements BuscarQuestaoNoQuestionarioPort {
    private final QuestaoRepositoryPort questaoRepository;

        public BuscarQuestaoNoQuestionarioService(QuestaoRepositoryPort questaoRepository) {
            this.questaoRepository = questaoRepository;
        }

        @Override
        public List<Questao> executar(Long idQuestionario) {
            return questaoRepository.buscarPorIdQuestionario(idQuestionario);
        }
}
