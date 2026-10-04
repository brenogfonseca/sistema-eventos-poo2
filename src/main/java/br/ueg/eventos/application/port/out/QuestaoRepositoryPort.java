package br.ueg.eventos.application.port.out;

import br.ueg.eventos.domain.model.Questao;
import java.util.List;
import java.util.Optional;

public interface QuestaoRepositoryPort {
    Questao salvar(Questao questao);
    Optional<Questao> buscarPorId(Long id);
    // Retorna uma lista, pois um questionário pode ter várias questões
    List<Questao> buscarPorIdQuestionario(Long idQuestionario); 
    void excluir(Long id);
}
