package br.ueg.eventos.application.port.in.atividade;

import br.ueg.eventos.domain.model.Atividade;
import java.util.List;

public interface ConsultarConflitosLocalAtividadePort {
    List<String> executar(Atividade atividade);
}
