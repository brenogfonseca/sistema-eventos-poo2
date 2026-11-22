package br.ueg.eventos.application.service.atividade;

import br.ueg.eventos.application.port.in.atividade.ConsultarConflitosLocalAtividadePort;
import br.ueg.eventos.application.port.out.AtividadeRepositoryPort;
import br.ueg.eventos.domain.model.Atividade;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

/** Finds room/time overlaps and reports them without rejecting the activity. */
public final class ConsultarConflitosLocalAtividadeService implements ConsultarConflitosLocalAtividadePort {
    private static final DateTimeFormatter HORARIO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final AtividadeRepositoryPort atividades;

    public ConsultarConflitosLocalAtividadeService(AtividadeRepositoryPort atividades) {
        this.atividades = atividades;
    }

    @Override
    public List<String> executar(Atividade nova) {
        if (nova == null || nova.getLocal() == null || nova.getLocal().getId() == null) return List.of();
        LocalDateTime inicioNovo = inicio(nova);
        LocalDateTime fimNovo = fim(nova);
        return atividades.listarPorLocal(nova.getLocal().getId()).stream()
                .filter(existente -> !Objects.equals(existente.getId(), nova.getId()))
                .filter(existente -> sobrepoe(inicioNovo, fimNovo, inicio(existente), fim(existente)))
                .map(existente -> "Já existe a atividade ‘" + existente.getNome() + "’ neste local, de "
                        + inicio(existente).format(HORARIO) + " a " + fim(existente).format(HORARIO)
                        + ". O cadastro foi permitido; confirme se a sala comporta as duas atividades.")
                .toList();
    }

    private LocalDateTime inicio(Atividade atividade) {
        return LocalDateTime.of(atividade.getData(), atividade.getHoraInicio());
    }

    private LocalDateTime fim(Atividade atividade) {
        LocalDateTime inicio = inicio(atividade);
        LocalDateTime fim = LocalDateTime.of(atividade.getData(), atividade.getHoraFim());
        return fim.isBefore(inicio) ? fim.plusDays(1) : fim;
    }

    private boolean sobrepoe(LocalDateTime inicioA, LocalDateTime fimA,
                             LocalDateTime inicioB, LocalDateTime fimB) {
        return inicioA.isBefore(fimB) && inicioB.isBefore(fimA);
    }
}
