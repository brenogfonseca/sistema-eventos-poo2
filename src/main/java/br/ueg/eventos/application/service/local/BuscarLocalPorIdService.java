package br.ueg.eventos.application.service.local;

import br.ueg.eventos.application.port.in.local.BuscarLocalPorIdPort;
import br.ueg.eventos.application.port.out.LocalRepositoryPort;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Local;

/**
 * Serviço de aplicação responsável pelo caso de uso de buscar um Local específico pelo seu ID.
 * Esta classe atua na camada de Application e implementa a porta de entrada (In Port) correspondente.
 */
public class BuscarLocalPorIdService implements BuscarLocalPorIdPort {

    // Porta de saída para acesso a dados, permitindo a inversão de dependência (Clean Architecture)
    private final LocalRepositoryPort localRepository;

    /**
     * Construtor para injeção de dependência.
     * Recebe a implementação do repositório em tempo de execução.
     *
     * @param localRepository interface do repositório do Local
     */
    public BuscarLocalPorIdService(LocalRepositoryPort localRepository) {
        this.localRepository = localRepository;
    }

    /**
     * Executa o caso de uso.
     *
     * @param id identificador único do Local a ser buscado
     * @return o Local encontrado no banco de dados
     * @throws RegraNegocioException caso não exista um Local com o ID fornecido
     */
    @Override
    public Local executar(Long id) {
        // Tenta buscar o Local no banco de dados através da porta do repositório.
        // O método 'buscarPorId' retorna um Optional, que ajuda a evitar NullPointerException.
        return localRepository.buscarPorId(id)
                // Se o Optional estiver vazio (ou seja, não encontrou o ID), lança uma exceção de negócio controlada.
                .orElseThrow(() -> new RegraNegocioException("Local não encontrado com o ID: " + id));
    }
}
