package br.ueg.eventos.application.port.in.frequencia;

import br.ueg.eventos.domain.model.Frequencia;

public interface RegistrarPresencaManualPort {
    Frequencia executar(Long idInscricaoEvento, Long idAtividade, boolean presente, Integer idResponsavel);
}
