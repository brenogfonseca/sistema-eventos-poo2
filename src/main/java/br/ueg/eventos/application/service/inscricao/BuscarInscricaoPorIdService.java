
package br.ueg.eventos.application.service.inscricao;

import br.ueg.eventos.application.port.in.inscricao.BuscarInscricaoPorIdPort;
import br.ueg.eventos.application.port.out.InscricaoRepositoryPort;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Inscricao;

public class BuscarInscricaoPorIdService implements BuscarInscricaoPorIdPort {

    private final InscricaoRepositoryPort inscricaoRepository;

    public BuscarInscricaoPorIdService(
            InscricaoRepositoryPort inscricaoRepository
    ) {
        this.inscricaoRepository = inscricaoRepository;
    }

    @Override
    public Inscricao executar(Long idInscricao) {
        if (idInscricao == null || idInscricao <= 0) {
            throw new RegraNegocioException(
                    "O id da inscrição deve ser válido."
            );
        }

        return inscricaoRepository.buscarPorId(idInscricao)
                .orElseThrow(() -> new RegraNegocioException(
                        "Inscrição não encontrada."
                ));
    }
}