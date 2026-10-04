package br.ueg.eventos.application.service.questao;

import br.ueg.eventos.application.port.in.questao.AtualizarQuestaoPort;
import br.ueg.eventos.application.port.out.QuestaoRepositoryPort;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Questao;
import java.util.Optional;

public class AtualizarQuestaoService implements AtualizarQuestaoPort {

    private final QuestaoRepositoryPort questaoRepository;

    public AtualizarQuestaoService(QuestaoRepositoryPort questaoRepository) {
        this.questaoRepository = questaoRepository;
    }

    @Override
    public Questao executar(ComandoAtualizarQuestao comando) {
        Optional<Questao> questaoExistente = questaoRepository.buscarPorId(comando.id);
        
        if (questaoExistente.isEmpty()) {
            throw new RegraNegocioException("Questão não encontrada para atualização.");
        }

        Questao questao = questaoExistente.get();
        questao.alterarEnunciado(comando.enunciado);
        questao.alterarTipo(comando.tipo);

        return questaoRepository.salvar(questao);
    }
}
