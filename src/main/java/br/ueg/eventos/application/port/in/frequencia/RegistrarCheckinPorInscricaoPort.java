package br.ueg.eventos.application.port.in.frequencia;

import br.ueg.eventos.domain.model.Frequencia;

/** Marca presença após o participante ter sido identificado por nome ou CPF. */
public interface RegistrarCheckinPorInscricaoPort {
    Frequencia executar(Long idInscricaoEvento, Long idAtividade, String acao, Integer idResponsavel);
}
