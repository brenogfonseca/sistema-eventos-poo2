package br.ueg.eventos.application.port.in.inscricao;

import br.ueg.eventos.domain.model.Inscricao;
import java.util.List;

public interface ListarAtividadesEscolhidasPort {
    List<Inscricao> executar(Long idInscricaoEvento, Long idUsuario);
}
