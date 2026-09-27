
package br.ueg.eventos.application.service.inscricao;

import br.ueg.eventos.application.port.in.inscricao.ReativarInscricaoPort;
import br.ueg.eventos.application.port.out.InscricaoRepositoryPort;
import br.ueg.eventos.application.port.out.AtividadeRepositoryPort;
import br.ueg.eventos.application.port.out.EventoRepositoryPort;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Inscricao;
import br.ueg.eventos.domain.model.Atividade;
import br.ueg.eventos.domain.model.Evento;

public class ReativarInscricaoService implements ReativarInscricaoPort {

    private final InscricaoRepositoryPort inscricaoRepository;
    private final AtividadeRepositoryPort atividadeRepository;
    private final EventoRepositoryPort eventoRepository;

    public ReativarInscricaoService(
            InscricaoRepositoryPort inscricaoRepository,
            AtividadeRepositoryPort atividadeRepository,
            EventoRepositoryPort eventoRepository
    ) {
        this.inscricaoRepository = inscricaoRepository;
        this.atividadeRepository = atividadeRepository;
        this.eventoRepository = eventoRepository;
    }

    @Override
    public Inscricao executar(Long idInscricao) {
        Inscricao inscricao = inscricaoRepository.buscarPorId(idInscricao)
                .orElseThrow(() -> new RegraNegocioException(
                        "Inscrição não encontrada."
                ));

        Atividade atividade = atividadeRepository
                .buscarPorId(inscricao.getIdAtividade())
                .orElseThrow(() -> new RegraNegocioException(
                        "Atividade não encontrada."
                ));

        Evento evento = eventoRepository.buscarPorId(atividade.getEventoId())
                .orElseThrow(() -> new RegraNegocioException(
                        "Evento não encontrado."
                ));

        if (evento.isDeletado()) {
            throw new RegraNegocioException(
                    "Não é possível reativar uma inscrição em um evento excluído."
            );
        }

        if (!evento.isInscricoesAbertas()) {
            throw new RegraNegocioException(
                    "As inscrições para este evento estão fechadas."
            );
        }

        boolean outraInscricaoAtiva = inscricaoRepository
                .listarPorAtividade(inscricao.getIdAtividade())
                .stream()
                .anyMatch(outra ->
                        !outra.getId().equals(inscricao.getId())
                        && outra.getIdUsuario().equals(inscricao.getIdUsuario())
                        && outra.estaAtiva()
                );

        if (outraInscricaoAtiva) {
            throw new RegraNegocioException(
                    "O usuário já possui outra inscrição ativa nesta atividade."
            );
        }

        atividade.registrarInscricao();
        inscricao.reativar();

        atividadeRepository.salvar(atividade);

        return inscricaoRepository.salvar(inscricao);
    }
}