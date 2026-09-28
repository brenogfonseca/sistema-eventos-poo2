package br.ueg.eventos.application.service.local;

import br.ueg.eventos.application.port.in.local.AlterarLocalPort;
import br.ueg.eventos.application.port.out.LocalRepositoryPort;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Local;

/**
 * Serviço de aplicação responsável por alterar os dados de um local existente.
 */
public class AlterarLocalService implements AlterarLocalPort {

    // Porta de saída injetada via construtor
    private final LocalRepositoryPort localRepository;

    public AlterarLocalService(LocalRepositoryPort localRepository) {
        this.localRepository = localRepository;
    }

    @Override
    public Local executar(ComandoAlterarLocal comando) {
        // Verifica se o local existe
        Local local = localRepository.buscarPorId(comando.id)
                .orElseThrow(() -> new RegraNegocioException("Local não encontrado com o ID: " + comando.id));

        // Validação de negócio
        if (comando.nome == null || comando.nome.trim().isEmpty()) {
            throw new RegraNegocioException("O nome do local é obrigatório.");
        }

        // Atualiza a entidade de domínio
        local.setNome(comando.nome);

        // Salva e retorna o local atualizado
        return localRepository.salvar(local);
    }
}
