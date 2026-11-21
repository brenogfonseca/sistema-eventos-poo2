package br.ueg.eventos.application.service.atividade;

import br.ueg.eventos.application.port.in.atividade.ListarAtividadesPorEventoPort;
import br.ueg.eventos.application.port.out.AtividadeRepositoryPort;
import br.ueg.eventos.application.port.out.EventoRepositoryPort;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Atividade;
import java.util.List;

public class ListarAtividadesPorEventoService
        implements ListarAtividadesPorEventoPort {

    private final AtividadeRepositoryPort atividadeRepository;

    public ListarAtividadesPorEventoService(AtividadeRepositoryPort atividadeRepository) {
        this.atividadeRepository = atividadeRepository;
    }

    /** Compatibilidade com a assinatura anterior; visibilidade pública é verificada no controller. */
    public ListarAtividadesPorEventoService(AtividadeRepositoryPort atividadeRepository,
                                            EventoRepositoryPort ignorado) {
        this(atividadeRepository);
    }

    @Override
    public List<Atividade> executar(Long idEvento) {
        if (idEvento == null || idEvento <= 0) {
            throw new RegraNegocioException(
                    "O id do evento deve ser válido.");
        }

        return atividadeRepository.listarPorEvento(idEvento);
    }
}
