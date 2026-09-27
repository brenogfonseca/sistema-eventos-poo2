package br.ueg.eventos.application.port.in.questao;

import br.ueg.eventos.domain.model.Questao;
import java.util.List;

public interface BuscarQuestaoNoQuestionarioPort {
    List<Questao> executar(Long idQuestionario);
}
