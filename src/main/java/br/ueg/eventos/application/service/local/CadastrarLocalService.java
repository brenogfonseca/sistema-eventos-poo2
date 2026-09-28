package br.ueg.eventos.application.service.local;

import br.ueg.eventos.application.port.in.local.CadastrarLocalPort;
import br.ueg.eventos.application.port.out.LocalRepositoryPort;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Local;

/**
 * Serviço de aplicação responsável pelo caso de uso de cadastrar um novo local.
 */
public class CadastrarLocalService implements CadastrarLocalPort {

    // Porta de saída injetada para persistência
    private final LocalRepositoryPort localRepository;

    public CadastrarLocalService(LocalRepositoryPort localRepository) {
        this.localRepository = localRepository;
    }

    @Override
    public Local executar(ComandoCadastrarLocal comando) {
        // Validação da regra de negócio: o nome não pode estar em branco
        if (comando.nome == null || comando.nome.trim().isEmpty()) {
            throw new RegraNegocioException("O nome do local é obrigatório.");
        }

        // Instancia a nova entidade do domínio
        Local novoLocal = new Local(comando.nome);

        // Persiste a entidade através da porta e retorna o resultado com o ID
        return localRepository.salvar(novoLocal);
    }
}
