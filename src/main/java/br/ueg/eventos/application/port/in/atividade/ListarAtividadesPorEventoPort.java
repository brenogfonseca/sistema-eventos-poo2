package br.ueg.eventos.application.port.in.atividade;

import br.ueg.eventos.domain.model.Atividade;
import java.util.List;

public interface ListarAtividadesPorEventoPort {
    List<Atividade> executar(Long idEvento);
}