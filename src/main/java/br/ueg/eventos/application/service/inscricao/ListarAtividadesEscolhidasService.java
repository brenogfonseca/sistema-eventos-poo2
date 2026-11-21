package br.ueg.eventos.application.service.inscricao;

import br.ueg.eventos.application.port.in.inscricao.ListarAtividadesEscolhidasPort;
import br.ueg.eventos.application.port.out.InscricaoEventoRepositoryPort;
import br.ueg.eventos.application.port.out.InscricaoRepositoryPort;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Inscricao;
import java.util.List;

public class ListarAtividadesEscolhidasService implements ListarAtividadesEscolhidasPort {
    private final InscricaoEventoRepositoryPort eventoRepository;
    private final InscricaoRepositoryPort atividadeRepository;
    public ListarAtividadesEscolhidasService(InscricaoEventoRepositoryPort eventoRepository, InscricaoRepositoryPort atividadeRepository) {
        this.eventoRepository = eventoRepository;
        this.atividadeRepository = atividadeRepository;
    }
    @Override public List<Inscricao> executar(Long idInscricaoEvento, Long idUsuario) {
        if (idInscricaoEvento == null || idInscricaoEvento <= 0) throw new RegraNegocioException("O id da inscrição no evento deve ser válido.");
        var inscricao = eventoRepository.buscarPorId(idInscricaoEvento).orElseThrow(() -> new RegraNegocioException("Inscrição no evento não encontrada."));
        if (idUsuario == null || !inscricao.getIdUsuario().equals(idUsuario)) throw new RegraNegocioException("Só é permitido consultar as próprias atividades escolhidas.");
        return atividadeRepository.listarPorInscricaoEvento(idInscricaoEvento);
    }
}
