package br.ueg.eventos.application.service.resposta;

import br.ueg.eventos.application.port.in.resposta.AtualizarRespostaPort;
import br.ueg.eventos.application.port.out.RespostaRepositoryPort;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Resposta;
import java.util.Optional;

public class AtualizarRespostaService implements AtualizarRespostaPort {

    private final RespostaRepositoryPort respostaRepository;

    public AtualizarRespostaService(RespostaRepositoryPort respostaRepository) {
        this.respostaRepository = respostaRepository;
    }

    @Override
    public Resposta executar(ComandoAtualizarResposta comando) {
        Optional<Resposta> respostaExistente = respostaRepository.buscarPorId(comando.id);
        
        if (respostaExistente.isEmpty()) {
            throw new RegraNegocioException("Resposta não encontrada para atualização.");
        }

        Resposta resposta = respostaExistente.get();
        if (comando.idExecutor == null || !resposta.getIdUsuario().equals(comando.idExecutor)) {
            throw new RegraNegocioException("Só é permitido alterar a própria resposta.");
        }
        // Utiliza o método de domínio que já contém a validação (não pode ser vazio)
        resposta.alterarValor(comando.valor);

        return respostaRepository.salvar(resposta);
    }
}
