package br.ueg.eventos.application.service.frequencia;

import br.ueg.eventos.application.port.in.frequencia.BuscarFrequenciaPorIdPort;
import br.ueg.eventos.application.port.out.FrequenciaRepositoryPort;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Frequencia;

public class BuscarFrequenciaPorIdService
        implements BuscarFrequenciaPorIdPort {

    private final FrequenciaRepositoryPort frequenciaRepository;

    public BuscarFrequenciaPorIdService(
            FrequenciaRepositoryPort frequenciaRepository
    ) {
        this.frequenciaRepository = frequenciaRepository;
    }

    @Override
    public Frequencia executar(Integer id) {
        if (id == null || id <= 0) {
            throw new RegraNegocioException(
                    "O id da frequência deve ser válido."
            );
        }

        return frequenciaRepository.buscarPorId(id)
                .orElseThrow(() -> new RegraNegocioException(
                        "Frequência não encontrada."
                ));
    }
}