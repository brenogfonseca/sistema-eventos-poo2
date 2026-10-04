package br.ueg.eventos.application.service.questionario;

import br.ueg.eventos.application.port.in.questionario.BuscarQuestionarioPort;
import br.ueg.eventos.application.port.out.QuestionarioRepositoryPort;
import br.ueg.eventos.domain.model.Questionario;

import java.util.Optional;

public class BuscarQuestionarioService implements BuscarQuestionarioPort {

    private final QuestionarioRepositoryPort questionarioRepository;

    // Construtor para injetar a dependência do repositório.
    public BuscarQuestionarioService(QuestionarioRepositoryPort questionarioRepository) {
        this.questionarioRepository = questionarioRepository;
    }

    // Retorna o questionário da atividade encapsulado em um Optional para respeitar a multiplicidade 0..1.
    @Override
    public Optional<Questionario> executar(Long idAtividade) {
        return questionarioRepository.buscarPorIdAtividade(idAtividade);
    }
}
