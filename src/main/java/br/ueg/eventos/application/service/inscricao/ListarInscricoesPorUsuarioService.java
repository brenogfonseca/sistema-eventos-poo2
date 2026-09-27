
package br.ueg.eventos.application.service.inscricao;

import br.ueg.eventos.application.port.in.inscricao.ListarInscricoesPorUsuarioPort;
import br.ueg.eventos.application.port.out.InscricaoRepositoryPort;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Inscricao;

import java.util.List;

public class ListarInscricoesPorUsuarioService
        implements ListarInscricoesPorUsuarioPort {

    private final InscricaoRepositoryPort inscricaoRepository;

    public ListarInscricoesPorUsuarioService(
            InscricaoRepositoryPort inscricaoRepository
    ) {
        this.inscricaoRepository = inscricaoRepository;
    }

    @Override
    public List<Inscricao> executar(Long idUsuario) {
        if (idUsuario == null || idUsuario <= 0) {
            throw new RegraNegocioException(
                    "O id do usuário deve ser válido."
            );
        }

        return inscricaoRepository.listarPorUsuario(idUsuario);
    }
}