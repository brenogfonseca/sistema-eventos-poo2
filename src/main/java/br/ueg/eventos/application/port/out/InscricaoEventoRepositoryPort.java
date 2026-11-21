package br.ueg.eventos.application.port.out;

import br.ueg.eventos.domain.model.InscricaoEvento;

import java.util.List;
import java.util.Optional;

public interface InscricaoEventoRepositoryPort {
    InscricaoEvento salvar(InscricaoEvento inscricao);
    Optional<InscricaoEvento> buscarPorId(Long id);
    Optional<InscricaoEvento> buscarPorEventoEUsuario(Long idEvento, Long idUsuario);
    List<InscricaoEvento> listarPorEvento(Long idEvento);
    List<InscricaoEvento> listarPorUsuario(Long idUsuario);
}
