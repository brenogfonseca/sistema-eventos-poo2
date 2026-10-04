package br.ueg.eventos.application.service.resposta;

import br.ueg.eventos.application.port.in.resposta.RemoverRespostaPort;
import br.ueg.eventos.application.port.out.RespostaRepositoryPort;

public class RemoverRespostaService implements RemoverRespostaPort {

    private final RespostaRepositoryPort respostaRepository;

    public RemoverRespostaService(RespostaRepositoryPort respostaRepository) {
        this.respostaRepository = respostaRepository;
    }

    @Override
    public void executar(Long id) {
        respostaRepository.excluir(id);
    }
}
