package br.ueg.eventos.application.port.in.evento;

import br.ueg.eventos.domain.model.Evento;
import java.util.List;

public interface ListarEventosPort {
    List<Evento> executar();
}
