package br.ueg.eventos.application.service.inscricao;

import br.ueg.eventos.application.port.in.inscricao.InscreverNoEventoPort;
import br.ueg.eventos.application.port.out.EventoRepositoryPort;
import br.ueg.eventos.application.port.out.InscricaoEventoRepositoryPort;
import br.ueg.eventos.application.port.out.UsuarioRepositoryPort;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Evento;
import br.ueg.eventos.domain.model.InscricaoEvento;

import java.time.LocalDateTime;

public class InscreverNoEventoService implements InscreverNoEventoPort {
    private final EventoRepositoryPort eventoRepository;
    private final InscricaoEventoRepositoryPort inscricaoRepository;
    private final UsuarioRepositoryPort usuarioRepository;

    public InscreverNoEventoService(EventoRepositoryPort eventoRepository,
                                    InscricaoEventoRepositoryPort inscricaoRepository,
                                    UsuarioRepositoryPort usuarioRepository) {
        this.eventoRepository = eventoRepository;
        this.inscricaoRepository = inscricaoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public InscricaoEvento executar(Long idEvento, Long idUsuario) {
        if (idEvento == null || idEvento <= 0 || idUsuario == null || idUsuario <= 0) {
            throw new RegraNegocioException("Evento e usuário devem ser válidos.");
        }
        int idUsuarioInt;
        try {
            idUsuarioInt = Math.toIntExact(idUsuario);
        } catch (ArithmeticException e) {
            throw new RegraNegocioException("O id do usuário excede o limite aceito pelo sistema.");
        }
        usuarioRepository.buscarPorId(idUsuarioInt)
                .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado."));
        Evento evento = eventoRepository.buscarPorId(idEvento)
                .orElseThrow(() -> new RegraNegocioException("Evento não encontrado."));
        validarEventoAberto(evento);

        var existente = inscricaoRepository.buscarPorEventoEUsuario(idEvento, idUsuario);
        if (existente.isPresent()) {
            InscricaoEvento inscricao = existente.get();
            if (inscricao.estaAtiva()) {
                return inscricao;
            }
            validarVaga(idEvento, evento);
            inscricao.reativar();
            return inscricaoRepository.salvar(inscricao);
        }

        validarVaga(idEvento, evento);
        return inscricaoRepository.salvar(new InscricaoEvento(idEvento, idUsuario));
    }

    private void validarVaga(Long idEvento, Evento evento) {
        long ativos = inscricaoRepository.listarPorEvento(idEvento).stream()
                .filter(InscricaoEvento::estaAtiva).count();
        if (ativos >= evento.getCapacidadeMaxima()) {
            throw new RegraNegocioException("Não há vagas disponíveis neste evento.");
        }
    }

    private void validarEventoAberto(Evento evento) {
        if (evento.isDeletado()) throw new RegraNegocioException("Não é possível se inscrever em um evento excluído.");
        if (!evento.isInscricoesAbertas()) throw new RegraNegocioException("As inscrições para este evento estão fechadas.");
        if (!evento.getPeriodo().getInicio().isAfter(LocalDateTime.now())) {
            throw new RegraNegocioException("Não é possível se inscrever depois que o evento começou.");
        }
    }
}
