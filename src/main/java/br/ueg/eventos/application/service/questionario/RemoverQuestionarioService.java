package br.ueg.eventos.application.service.questionario;

import br.ueg.eventos.application.port.in.questionario.RemoverQuestionarioPort;
import br.ueg.eventos.application.port.out.QuestionarioRepository;

public class RemoverQuestionarioService implements RemoverQuestionarioPort {

    private final QuestionarioRepository questionarioRepository;

    // Construtor para injetar a dependência do repositório.
    public RemoverQuestionarioService(QuestionarioRepository questionarioRepository) {
        this.questionarioRepository = questionarioRepository;
    }

    // Remove um questionário existente repassando o ID para o adaptador de banco de dados.
    @Override
    public void executar(Long id) {
        questionarioRepository.excluir(id);
    }
}