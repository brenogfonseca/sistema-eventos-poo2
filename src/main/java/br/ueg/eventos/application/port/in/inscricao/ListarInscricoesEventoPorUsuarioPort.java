package br.ueg.eventos.application.port.in.inscricao;

import br.ueg.eventos.domain.model.InscricaoEvento;
import java.util.List;

public interface ListarInscricoesEventoPorUsuarioPort {
    List<InscricaoEvento> executar(Long idUsuario);
}
