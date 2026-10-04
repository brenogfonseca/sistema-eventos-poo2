package br.ueg.eventos.application.service.resposta;

import br.ueg.eventos.application.port.in.resposta.RemoverRespostaPort;
import br.ueg.eventos.application.port.out.RespostaRepositoryPort;

public class RemoverRespostaService implements RemoverRespostaPort {

    private final RespostaRepository respostaRepository;

    public RemoverRespostaService(RespostaRepository respostaRepository) {
        this.respostaRepository = respostaRepository;
    }

    @Override
    public void executar(Long id) {
        respostaRepository.excluir(id);
    }
}
