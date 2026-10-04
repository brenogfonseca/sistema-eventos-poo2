package br.ueg.eventos.application.service.resposta;

import br.ueg.eventos.application.port.in.resposta.BuscarRespostaPort;
import br.ueg.eventos.application.port.out.RespostaRepositoryPort;
import br.ueg.eventos.domain.model.Resposta;
import java.util.List;

public class BuscarRespostaService implements BuscarRespostaPort {

    private final RespostaRepositoryPort respostaRepository;

    public BuscarRespostaService(RespostaRepositoryPort respostaRepository) {
        this.respostaRepository = respostaRepository;
    }

    @Override
    public List<Resposta> executar(Long idQuestao) {
        return respostaRepository.buscarPorIdQuestao(idQuestao);
    }
}
