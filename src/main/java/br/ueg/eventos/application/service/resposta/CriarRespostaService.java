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
        // Cria a entidade validando as regras de negócio no construtor
        Resposta resposta = new Resposta(comando.idQuestao, comando.idUsuario, comando.valor);
        return respostaRepository.salvar(resposta);
    }
}
