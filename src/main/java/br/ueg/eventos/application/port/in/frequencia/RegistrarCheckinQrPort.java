package br.ueg.eventos.application.port.in.frequencia;

import br.ueg.eventos.domain.model.Frequencia;

public interface RegistrarCheckinQrPort {

    Frequencia executar(String token, Long idAtividade, String acao, Integer idResponsavel);
}
