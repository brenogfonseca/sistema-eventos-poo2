package br.ueg.eventos.application.service.frequencia;

import br.ueg.eventos.application.port.in.frequencia.ListarFrequenciasPorInscricaoPort;
import br.ueg.eventos.application.port.out.FrequenciaRepositoryPort;
import br.ueg.eventos.application.port.out.InscricaoRepositoryPort;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Frequencia;

import java.util.List;

public class ListarFrequenciasPorInscricaoService
        implements ListarFrequenciasPorInscricaoPort {

    private final FrequenciaRepositoryPort frequenciaRepository;
    private final InscricaoRepositoryPort inscricaoRepository;

    public ListarFrequenciasPorInscricaoService(
            FrequenciaRepositoryPort frequenciaRepository,
            InscricaoRepositoryPort inscricaoRepository
    ) {
        this.frequenciaRepository = frequenciaRepository;
        this.inscricaoRepository = inscricaoRepository;
    }

    @Override
    public List<Frequencia> executar(Long idInscricao) {
        if (idInscricao == null || idInscricao <= 0) {
            throw new RegraNegocioException(
                    "O id da inscrição deve ser válido."
            );
        }

        int idInscricaoInt;
        try {
            idInscricaoInt = Math.toIntExact(idInscricao);
        } catch (ArithmeticException e) {
            throw new RegraNegocioException(
                    "O id da inscrição excede o limite aceito pelo modelo de frequência."
            );
        }

        inscricaoRepository.buscarPorId(idInscricao)
                .orElseThrow(() -> new RegraNegocioException(
                        "Inscrição não encontrada."
                ));

        return frequenciaRepository.listarPorInscricao(idInscricaoInt);
    }
}