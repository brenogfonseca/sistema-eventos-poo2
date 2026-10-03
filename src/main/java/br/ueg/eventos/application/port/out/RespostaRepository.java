package br.ueg.eventos.application.port.out;

import br.ueg.eventos.domain.model.Resposta;
import java.util.List;
import java.util.Optional;

public interface RespostaRepository {
    Resposta salvar(Resposta resposta);
    Optional<Resposta> buscarPorId(Long id);
    // Retorna a lista de respostas vinculadas a uma questão
    List<Resposta> buscarPorIdQuestao(Long idQuestao); 
    void excluir(Long id);
}