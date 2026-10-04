package br.ueg.eventos.application.service.questionario;

import br.ueg.eventos.application.port.in.questionario.AtualizarQuestionarioPort;
import br.ueg.eventos.application.port.out.QuestionarioRepositoryPort;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Questionario;

import java.util.Optional;

public class AtualizarQuestionarioService implements AtualizarQuestionarioPort {

    private final QuestionarioRepositoryPort questionarioRepository;

    // Construtor para injetar a dependência do repositório.
    public AtualizarQuestionarioService(QuestionarioRepositoryPort questionarioRepository) {
        this.questionarioRepository = questionarioRepository;
    }

    // Atualiza os dados de um questionário previamente existente.
    @Override
    public Questionario executar(ComandoAtualizarQuestionario comando) {
        Optional<Questionario> questionarioExistente = questionarioRepository.buscarPorId(comando.id);
        
        if (questionarioExistente.isEmpty()) {
            throw new RegraNegocioException("Questionário não encontrado para atualização.");
        }

        Questionario questionario = questionarioExistente.get();
        
        // Altera o título utilizando o método que contém a validação interna da entidade.
        questionario.alterarTitulo(comando.titulo);

        return questionarioRepository.salvar(questionario);
    }
}
