
package br.ueg.eventos.application.service.inscricao;

import br.ueg.eventos.application.port.in.inscricao.InscreverUsuarioPort;
import br.ueg.eventos.application.port.out.InscricaoRepositoryPort;
import br.ueg.eventos.application.port.out.AtividadeRepositoryPort;
import br.ueg.eventos.application.port.out.EventoRepositoryPort;
import br.ueg.eventos.application.port.out.UsuarioRepositoryPort;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Inscricao;
import br.ueg.eventos.domain.model.Atividade;
import br.ueg.eventos.domain.model.Evento;

public class InscreverUsuarioService implements InscreverUsuarioPort {

    private final InscricaoRepositoryPort inscricaoRepository;
    private final AtividadeRepositoryPort atividadeRepository;
    private final EventoRepositoryPort eventoRepository;
    private final UsuarioRepositoryPort usuarioRepository;

    public InscreverUsuarioService(
            InscricaoRepositoryPort inscricaoRepository,
            AtividadeRepositoryPort atividadeRepository,
            EventoRepositoryPort eventoRepository
    ) {
        this(
                inscricaoRepository,
                atividadeRepository,
                eventoRepository,
                null
        );
    }

    public InscreverUsuarioService(
            InscricaoRepositoryPort inscricaoRepository,
            AtividadeRepositoryPort atividadeRepository,
            EventoRepositoryPort eventoRepository,
            UsuarioRepositoryPort usuarioRepository
    ) {
        this.inscricaoRepository = inscricaoRepository;
        this.atividadeRepository = atividadeRepository;
        this.eventoRepository = eventoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public Inscricao executar(Long idAtividade, Long idUsuario) {
        validarUsuarioSeRepositorioDisponivel(idUsuario);

        Atividade atividade = atividadeRepository.buscarPorId(idAtividade)
                .orElseThrow(() -> new RegraNegocioException(
                        "Atividade não encontrada."
                ));

        Evento evento = eventoRepository.buscarPorId(atividade.getEventoId())
                .orElseThrow(() -> new RegraNegocioException(
                        "Evento não encontrado."
                ));

        if (evento.isDeletado()) {
            throw new RegraNegocioException(
                    "Não é possível se inscrever em um evento excluído."
            );
        }

        if (!evento.isInscricoesAbertas()) {
            throw new RegraNegocioException(
                    "As inscrições para este evento estão fechadas."
            );
        }

        boolean jaInscrito = inscricaoRepository
                .listarPorAtividade(idAtividade)
                .stream()
                .anyMatch(inscricao ->
                        inscricao.getIdUsuario().equals(idUsuario)
                        && inscricao.estaAtiva()
                );

        if (jaInscrito) {
            throw new RegraNegocioException(
                    "O usuário já possui uma inscrição ativa nesta atividade."
            );
        }

        atividade.registrarInscricao();

        Inscricao inscricao = new Inscricao(idAtividade, idUsuario);

        atividadeRepository.salvar(atividade);

        return inscricaoRepository.salvar(inscricao);
    }

    private void validarUsuarioSeRepositorioDisponivel(Long idUsuario) {
        if (usuarioRepository == null) {
            return;
        }

        if (idUsuario == null || idUsuario <= 0) {
            throw new RegraNegocioException(
                    "O id do usuário deve ser válido."
            );
        }

        final int idUsuarioInt;
        try {
            idUsuarioInt = Math.toIntExact(idUsuario);
        } catch (ArithmeticException e) {
            throw new RegraNegocioException(
                    "O id do usuário excede o limite aceito pelo sistema."
            );
        }

        usuarioRepository.buscarPorId(idUsuarioInt)
                .orElseThrow(() -> new RegraNegocioException(
                        "Usuário não encontrado."
                ));
    }
}
