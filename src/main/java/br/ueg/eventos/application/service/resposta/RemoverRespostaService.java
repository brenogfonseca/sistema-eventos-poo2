package br.ueg.eventos.application.service.resposta;

import br.ueg.eventos.application.port.in.resposta.RemoverRespostaPort;
import br.ueg.eventos.application.port.out.RespostaRepositoryPort;
import br.ueg.eventos.domain.exception.RegraNegocioException;

public class RemoverRespostaService implements RemoverRespostaPort {

    private final RespostaRepositoryPort respostaRepository;

    public RemoverRespostaService(RespostaRepositoryPort respostaRepository) {
        this.respostaRepository = respostaRepository;
    }

    @Override
    public void executar(Long id, Integer idExecutor) {
        var resposta = respostaRepository.buscarPorId(id)
                .orElseThrow(() -> new RegraNegocioException("Resposta não encontrada."));
        if (idExecutor == null || !resposta.getIdUsuario().equals(idExecutor)) {
            throw new RegraNegocioException("Só é permitido remover a própria resposta.");
        }
        respostaRepository.excluir(id);
    }
}
