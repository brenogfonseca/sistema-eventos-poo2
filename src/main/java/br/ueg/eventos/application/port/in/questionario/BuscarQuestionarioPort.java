package br.ueg.eventos.application.port.in.questionario;

import br.ueg.eventos.domain.model.Questionario;
import java.util.Optional;

public interface BuscarQuestionarioPort {
    Optional<Questionario> executar(Long idAtividade);
}
