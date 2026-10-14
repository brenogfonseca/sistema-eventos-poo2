package br.ueg.eventos.application.service.atividade;

import br.ueg.eventos.application.port.in.atividade.AtualizarAtividadePort;
import br.ueg.eventos.application.port.out.AtividadeRepositoryPort;
import br.ueg.eventos.application.port.out.EventoRepositoryPort;
import br.ueg.eventos.application.port.out.TipoRepositoryPort;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Atividade;
import br.ueg.eventos.domain.model.Evento;
import br.ueg.eventos.domain.model.Tipo;

public class AtualizarAtividadeService
        implements AtualizarAtividadePort {

    private final AtividadeRepositoryPort atividadeRepository;
    private final EventoRepositoryPort eventoRepository;
    private final TipoRepositoryPort tipoRepository;

 public AtualizarAtividadeService(
         AtividadeRepositoryPort atividadeRepository,
         EventoRepositoryPort eventoRepository,
         TipoRepositoryPort tipoRepository) {
         this.atividadeRepository = atividadeRepository;
         this.eventoRepository = eventoRepository;
         this.tipoRepository = tipoRepository;
 }

    @Override
    public Atividade executar(Long id, Atividade atividade) {
        if (id == null || id <= 0) {
            throw new RegraNegocioException(
                    "O id da atividade deve ser válido.");
        }

        if (atividade == null) {
            throw new RegraNegocioException(
                    "A atividade é obrigatória.");
        }

        atividadeRepository.buscarPorId(id)
                .orElseThrow(() -> new RegraNegocioException(
                        "Atividade não encontrada."));

        Evento evento = eventoRepository
                .buscarPorId(atividade.getEventoId())
                .orElseThrow(() -> new RegraNegocioException(
                        "Evento não encontrado."));

        if (evento.isDeletado()) {
            throw new RegraNegocioException(
                    "Não é possível vincular a atividade a um evento excluído.");
        }

        if (atividade.getId() != null
                && !id.equals(atividade.getId())) {
            throw new RegraNegocioException(
                    "O id da atividade não corresponde ao id informado.");
        }

            // Confirma que o tipo informado existe.
        Tipo tipoInformado = atividade.getTipo();
        if (tipoInformado == null || tipoInformado.getId() == null) {
            throw new RegraNegocioException(
                    "O tipo da atividade é obrigatório."
            );
        }

        Tipo tipoCadastrado = tipoRepository
                .buscarPorId(tipoInformado.getId())
                .orElseThrow(() -> new RegraNegocioException(
                        "Tipo não encontrado."
                ));

        atividade.setTipo(tipoCadastrado);

        return atividadeRepository.salvar(atividade);
    }
}
