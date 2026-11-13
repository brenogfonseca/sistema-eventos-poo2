package br.ueg.eventos.application.service.inscricao;

import br.ueg.eventos.application.port.in.inscricao.ListarInscricoesEventoPorUsuarioPort;
import br.ueg.eventos.application.port.out.InscricaoEventoRepositoryPort;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.InscricaoEvento;
import java.util.List;

public class ListarInscricoesEventoPorUsuarioService implements ListarInscricoesEventoPorUsuarioPort {
    private final InscricaoEventoRepositoryPort repository;
    public ListarInscricoesEventoPorUsuarioService(InscricaoEventoRepositoryPort repository) { this.repository = repository; }
    @Override public List<InscricaoEvento> executar(Long idUsuario) {
        if (idUsuario == null || idUsuario <= 0) throw new RegraNegocioException("O id do usuário deve ser válido.");
        return repository.listarPorUsuario(idUsuario);
    }
}
