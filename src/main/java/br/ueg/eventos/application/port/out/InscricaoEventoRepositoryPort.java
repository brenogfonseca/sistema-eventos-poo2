package br.ueg.eventos.application.port.out;

import br.ueg.eventos.domain.model.InscricaoEvento;
import br.ueg.eventos.domain.exception.RegraNegocioException;

import java.util.List;
import java.util.Optional;

public interface InscricaoEventoRepositoryPort {
    InscricaoEvento salvar(InscricaoEvento inscricao);
    Optional<InscricaoEvento> buscarPorId(Long id);
    Optional<InscricaoEvento> buscarPorEventoEUsuario(Long idEvento, Long idUsuario);
    List<InscricaoEvento> listarPorEvento(Long idEvento);
    List<InscricaoEvento> listarPorUsuario(Long idUsuario);

    /** Creates/reactivates one enrollment while checking event capacity atomically. */
    default InscricaoEvento inscreverSeDisponivel(Long idEvento, Long idUsuario, int capacidade) {
        var existente = buscarPorEventoEUsuario(idEvento, idUsuario);
        if (existente.isPresent() && existente.get().estaAtiva()) return existente.get();
        long ativos = listarPorEvento(idEvento).stream().filter(InscricaoEvento::estaAtiva).count();
        if (ativos >= capacidade) throw new RegraNegocioException("Não há vagas disponíveis neste evento.");
        if (existente.isPresent()) {
            InscricaoEvento reativada = existente.get();
            reativada.reativar();
            return salvar(reativada);
        }
        return salvar(new InscricaoEvento(idEvento, idUsuario));
    }
}
