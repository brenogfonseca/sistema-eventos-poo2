package br.ueg.eventos.application.service.evento;

import br.ueg.eventos.application.port.in.evento.CriarEventoPort;
import br.ueg.eventos.application.port.out.EventoRepositoryPort;
import br.ueg.eventos.application.port.out.VinculoOrganizadorRepositoryPort;
import br.ueg.eventos.domain.model.Evento;
import br.ueg.eventos.domain.model.Periodo;
import br.ueg.eventos.domain.model.VinculoOrganizador;
import br.ueg.eventos.domain.exception.RegraNegocioException;

import java.util.List;

public class CriarEventoService implements CriarEventoPort {

    private final EventoRepositoryPort eventoRepository;
    private final VinculoOrganizadorRepositoryPort vinculoOrganizadorRepository;

    // Construtor com VinculoOrganizadorRepositoryPort
    public CriarEventoService(EventoRepositoryPort eventoRepository,
                              VinculoOrganizadorRepositoryPort vinculoOrganizadorRepository) {
        this.eventoRepository = eventoRepository;
        this.vinculoOrganizadorRepository = vinculoOrganizadorRepository;
    }

    // Construtor de compatibilidade caso chamado apenas com EventoRepositoryPort
    public CriarEventoService(EventoRepositoryPort eventoRepository) {
        this(eventoRepository, null);
    }

    @Override
    public Evento executar(ComandoCriarEvento comando) {
        Periodo periodo = new Periodo(comando.inicio, comando.fim);

        List<Evento> eventosExistentes = eventoRepository.listarTodos();
        for (Evento e : eventosExistentes) {
            // Apenas verifica conflito com eventos ativos
            if (!e.isDeletado() && e.getPeriodo().conflitaCom(periodo)) {
                throw new RegraNegocioException("Já existe um evento marcado para este período.");
            }
        }

        Evento evento = new Evento(comando.titulo, comando.descricao, periodo, comando.capacidade);
        Evento eventoSalvo = eventoRepository.salvar(evento);

        // Se foi informado o criador/organizador e o repositório estiver presente, registra o vínculo associativo
        if (comando.idUsuarioCriador != null && vinculoOrganizadorRepository != null) {
            VinculoOrganizador vinculo = new VinculoOrganizador(
                    comando.idUsuarioCriador,
                    eventoSalvo.getId().intValue(),
                    true // Define como Criador/Dono Principal
            );
            vinculoOrganizadorRepository.salvar(vinculo);
        }

        return eventoSalvo;
    }
}

