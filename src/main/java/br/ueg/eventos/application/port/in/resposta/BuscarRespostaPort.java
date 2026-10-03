package br.ueg.eventos.application.port.in.resposta;

import br.ueg.eventos.domain.model.Resposta;
import java.util.List;

public interface BuscarRespostaPort {
    List<Resposta> executar(Long idQuestao);
}
