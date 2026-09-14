package br.ueg.eventos.application.service.evento;

import br.ueg.eventos.application.port.in.evento.CriarEventoPort;
import br.ueg.eventos.application.port.out.EventoRepository;
import br.ueg.eventos.domain.model.Evento;
import br.ueg.eventos.domain.model.Periodo;
import br.ueg.eventos.domain.exception.RegraNegocioException;

import java.util.List;

public class CriarEventoService implements CriarEventoPort {

    private final EventoRepository eventoRepository;

    public CriarEventoService(EventoRepository eventoRepository) {
        this.eventoRepository = eventoRepository;
    }

    @Override
    public Evento executar(ComandoCriarEvento comando) {
        Periodo periodo = new Periodo(comando.inicio, comando.fim);

        List<Evento> eventosExistentes = eventoRepository.listarTodos();
        for (Evento e : eventosExistentes) {
            if (e.getPeriodo().conflitaCom(periodo)) {
                throw new RegraNegocioException("Já existe um evento marcado para este período.");
            }
        }

        Evento evento = new Evento(comando.titulo, comando.descricao, periodo, comando.capacidade);

        return eventoRepository.salvar(evento);
    }
}
