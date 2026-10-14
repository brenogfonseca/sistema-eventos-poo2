package br.ueg.eventos.application.service.trilha;

import br.ueg.eventos.application.port.in.trilha.AlterarTrilhaPort;
import br.ueg.eventos.application.port.out.TrilhaRepositoryPort;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Trilha;

/** Serviço de alteração de trilha. */
public class AlterarTrilhaService implements AlterarTrilhaPort {

    private final TrilhaRepositoryPort trilhaRepository;

    public AlterarTrilhaService(TrilhaRepositoryPort trilhaRepository) {
        this.trilhaRepository = trilhaRepository;
    }

    @Override
    public Trilha executar(ComandoAlterarTrilha comando) {
        if (comando == null || comando.id == null || comando.id <= 0) {
            throw new RegraNegocioException("ID da trilha inválido.");
        }
        if (comando.nome == null || comando.nome.isBlank()) {
            throw new RegraNegocioException("O nome da trilha é obrigatório.");
        }

        // Confirma que a trilha existe antes de alterá-la.
        Trilha trilha = trilhaRepository.buscarPorId(comando.id)
                .orElseThrow(() ->
                        new RegraNegocioException("Trilha não encontrada."));

        trilha.setNome(comando.nome.trim());
        return trilhaRepository.salvar(trilha);
    }
}