package br.ueg.eventos.application.service.questionario;

import br.ueg.eventos.application.port.in.questionario.AtualizarQuestionarioPort;
import br.ueg.eventos.application.port.out.QuestionarioRepositoryPort;
import br.ueg.eventos.application.port.out.QuestaoRepositoryPort;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Questionario;

import java.util.Optional;

public class AtualizarQuestionarioService implements AtualizarQuestionarioPort {

    private final QuestionarioRepositoryPort questionarioRepository;
    private final QuestaoRepositoryPort questaoRepository;

    // Construtor para injetar a dependência do repositório.
    public AtualizarQuestionarioService(QuestionarioRepositoryPort questionarioRepository,
                                        QuestaoRepositoryPort questaoRepository) {
        this.questionarioRepository = questionarioRepository;
        this.questaoRepository = questaoRepository;
    }

    // Atualiza os dados de um questionário previamente existente.
    @Override
    public Questionario executar(ComandoAtualizarQuestionario comando) {
        Optional<Questionario> questionarioExistente = questionarioRepository.buscarPorId(comando.id);
        
        if (questionarioExistente.isEmpty()) {
            throw new RegraNegocioException("Questionário não encontrado para atualização.");
        }

        Questionario questionario = questionarioExistente.get();
        if (comando.publicado && questaoRepository.buscarPorIdQuestionario(comando.id).isEmpty()) {
            throw new RegraNegocioException("Cadastre ao menos uma questão antes de publicar a avaliação.");
        }
        
        // Altera o título utilizando o método que contém a validação interna da entidade.
        questionario.alterarTitulo(comando.titulo);
        questionario.configurarAvaliacao(comando.anonimo, comando.publicado);

        return questionarioRepository.salvar(questionario);
    }
}
