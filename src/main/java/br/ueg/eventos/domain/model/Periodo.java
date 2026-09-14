package br.ueg.eventos.domain.model;

import br.ueg.eventos.domain.exception.RegraNegocioException;
import java.time.LocalDateTime;

/**
 * Value Object representando um período de tempo (data início e data fim).
 * Atende ao requisito ROO-03 (Objetos de Valor).
 */
public class Periodo {
    
    private final LocalDateTime inicio;
    private final LocalDateTime fim;

    public Periodo(LocalDateTime inicio, LocalDateTime fim) {
        if (inicio == null || fim == null) {
            throw new RegraNegocioException("As datas de início e fim não podem ser nulas.");
        }
        if (inicio.isAfter(fim)) {
            throw new RegraNegocioException("A data de início não pode ser posterior à data de término.");
        }
        
        this.inicio = inicio;
        this.fim = fim;
    }

    public LocalDateTime getInicio() {
        return inicio;
    }

    public LocalDateTime getFim() {
        return fim;
    }

    // Comportamento: verifica se este período conflita (sobrepõe) com outro
    public boolean conflitaCom(Periodo outro) {
        return this.inicio.isBefore(outro.fim) && outro.inicio.isBefore(this.fim);
    }
}
