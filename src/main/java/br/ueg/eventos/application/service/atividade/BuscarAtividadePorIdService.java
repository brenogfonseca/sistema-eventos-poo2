package br.ueg.eventos.application.service.atividade;

import br.ueg.eventos.application.port.in.atividade.BuscarAtividadePorIdPort;
import br.ueg.eventos.application.port.out.AtividadeRepositoryPort;
import br.ueg.eventos.application.port.out.EventoRepositoryPort;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Atividade;

public class BuscarAtividadePorIdService
        implements BuscarAtividadePorIdPort {

    private final AtividadeRepositoryPort atividadeRepository;
    private final EventoRepositoryPort eventoRepository;

    public BuscarAtividadePorIdService(
            AtividadeRepositoryPort atividadeRepository,
            EventoRepositoryPort eventoRepository) {
        this.atividadeRepository = atividadeRepository;
        this.eventoRepository = eventoRepository;
    }

    @Override
    public Atividade executar(Long id) {
        if (id == null || id <= 0) {
            throw new RegraNegocioException(
                    "O id da atividade deve ser válido.");
        }

        Atividade atividade = atividadeRepository.buscarPorId(id)
                .orElseThrow(() -> new RegraNegocioException("Atividade não encontrada."));
        var evento = eventoRepository.buscarPorId(atividade.getEventoId())
                .orElseThrow(() -> new RegraNegocioException("Atividade não encontrada."));
        if (evento.isDeletado()) {
            throw new RegraNegocioException("Atividade não encontrada.");
        }
        return atividade;
    }
}
