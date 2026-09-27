package br.ueg.eventos.application.service.frequencia;

import br.ueg.eventos.application.port.in.frequencia.AtualizarPresencaPort;
import br.ueg.eventos.application.port.out.FrequenciaRepositoryPort;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Frequencia;

public class AtualizarPresencaService implements AtualizarPresencaPort {

    private final FrequenciaRepositoryPort frequenciaRepository;

    public AtualizarPresencaService(
            FrequenciaRepositoryPort frequenciaRepository
    ) {
        this.frequenciaRepository = frequenciaRepository;
    }

    @Override
    public Frequencia executar(Integer idFrequencia, boolean presente) {
        if (idFrequencia == null || idFrequencia <= 0) {
            throw new RegraNegocioException(
                    "O id da frequência deve ser válido."
            );
        }

        Frequencia frequencia = frequenciaRepository
                .buscarPorId(idFrequencia)
                .orElseThrow(() -> new RegraNegocioException(
                        "Frequência não encontrada."
                ));

        if (presente) {
            frequencia.registrarPresenca();
        } else {
            frequencia.registrarAusencia();
        }

        return frequenciaRepository.salvar(frequencia);
    }
}