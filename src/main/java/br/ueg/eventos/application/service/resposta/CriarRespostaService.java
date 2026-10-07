package br.ueg.eventos.application.service.resposta;

import br.ueg.eventos.application.port.in.resposta.CriarRespostaPort;
import br.ueg.eventos.application.port.out.RespostaRepositoryPort;
import br.ueg.eventos.domain.model.Resposta;

public class CriarRespostaService implements CriarRespostaPort {

    private final RespostaRepositoryPort respostaRepository;

    public CriarRespostaService(RespostaRepositoryPort respostaRepository) {
        this.respostaRepository = respostaRepository;
    }

    @Override
    public Resposta executar(ComandoCriarResposta comando) {
        Integer idQuestao = comando.idQuestao != null ? comando.idQuestao.intValue() : null;
        Resposta resposta = new Resposta(idQuestao, comando.idUsuario, comando.valor);
        return respostaRepository.salvar(resposta);
    }
}
