package br.ueg.eventos.application.service.questionario;

import br.ueg.eventos.application.port.in.questionario.CriarQuestionarioPort;
import br.ueg.eventos.application.port.out.QuestionarioRepository;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Questionario;

import java.util.Optional;

public class CriarQuestionarioService implements CriarQuestionarioPort {

    private final QuestionarioRepository questionarioRepository;

    // Construtor para injetar a dependência do repositório.
    public CriarQuestionarioService(QuestionarioRepository questionarioRepository) {
        this.questionarioRepository = questionarioRepository;
    }

    // Executa a criação garantindo a regra de negócio de no máximo um questionário por atividade.
    @Override
    public Questionario executar(ComandoCriarQuestionario comando) {
        Optional<Questionario> questionarioExistente = questionarioRepository.buscarPorIdAtividade(comando.idAtividade);
        
        if (questionarioExistente.isPresent()) {
            throw new RegraNegocioException("Esta atividade já possui um questionário vinculado. Apenas um é permitido.");
        }

        Questionario questionario = new Questionario(comando.idAtividade, comando.titulo);

        return questionarioRepository.salvar(questionario);
    }
}