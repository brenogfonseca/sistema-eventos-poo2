package br.ueg.eventos.application.port.in.frequencia;

import java.util.List;

public interface BuscarParticipantesCheckinPort {
    List<Participante> executar(Long idAtividade, String termo);

    record Participante(Long idInscricaoEvento, String nome, String cpfMascarado) { }
}
