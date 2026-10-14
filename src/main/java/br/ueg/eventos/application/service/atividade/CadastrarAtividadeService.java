package br.ueg.eventos.application.service.atividade;

import br.ueg.eventos.application.port.in.atividade.CadastrarAtividadePort;
import br.ueg.eventos.application.port.out.AtividadeRepositoryPort;
import br.ueg.eventos.application.port.out.EventoRepositoryPort;
import br.ueg.eventos.application.port.out.TipoRepositoryPort;
import br.ueg.eventos.domain.model.Tipo;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Atividade;
import br.ueg.eventos.domain.model.Evento;

public class CadastrarAtividadeService
        implements CadastrarAtividadePort {

    private final AtividadeRepositoryPort atividadeRepository;
    private final EventoRepositoryPort eventoRepository;
    private final TipoRepositoryPort tipoRepository;

 public CadastrarAtividadeService(
         AtividadeRepositoryPort atividadeRepository,
         EventoRepositoryPort eventoRepository,
         TipoRepositoryPort tipoRepository) {
     this.atividadeRepository = atividadeRepository;
     this.eventoRepository = eventoRepository;
     this.tipoRepository = tipoRepository;
 }

    @Override
    public Atividade executar(Atividade atividade) {
        if (atividade == null) {
            throw new RegraNegocioException(
                    "A atividade é obrigatória.");
        }

        Evento evento = eventoRepository
                .buscarPorId(atividade.getEventoId())
                .orElseThrow(() -> new RegraNegocioException(
                        "Evento não encontrado."));

        if (evento.isDeletado()) {
            throw new RegraNegocioException(
                    "Não é possível cadastrar atividades em um evento excluído.");
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
