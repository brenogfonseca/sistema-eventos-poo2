package br.ueg.eventos.application.service.trilha;

import br.ueg.eventos.application.port.in.trilha.CadastrarTrilhaPort;
import br.ueg.eventos.application.port.out.TrilhaRepositoryPort;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Trilha;

/** Serviço de cadastro de trilha. */
public class CadastrarTrilhaService implements CadastrarTrilhaPort {

    private final TrilhaRepositoryPort trilhaRepository;

    public CadastrarTrilhaService(TrilhaRepositoryPort trilhaRepository) {
        this.trilhaRepository = trilhaRepository;
    }

    @Override
    public Trilha executar(ComandoCadastrarTrilha comando) {
        // Verifica se o nome foi informado.
        if (comando == null || comando.nome == null || comando.nome.isBlank()) {
            throw new RegraNegocioException("O nome da trilha é obrigatório.");
        }

        return trilhaRepository.salvar(new Trilha(comando.nome.trim()));
    }
}