package br.ueg.eventos.application.service.local;

import br.ueg.eventos.application.port.in.local.DeletarLocalPort;
import br.ueg.eventos.application.port.out.LocalRepositoryPort;
import br.ueg.eventos.domain.exception.RegraNegocioException;

/**
 * Serviço de aplicação responsável por deletar um local de forma lógica/física dependendo do repositório.
 */
public class DeletarLocalService implements DeletarLocalPort {

    private final LocalRepositoryPort localRepository;

    public DeletarLocalService(LocalRepositoryPort localRepository) {
        this.localRepository = localRepository;
    }

    @Override
    public void executar(Long id) {
        // Verifica se o local existe antes de tentar deletar
        localRepository.buscarPorId(id)
                .orElseThrow(() -> new RegraNegocioException("Local não encontrado com o ID: " + id));

        // Realiza a exclusão através da porta do repositório
        localRepository.deletar(id);
    }
}
