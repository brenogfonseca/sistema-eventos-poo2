package br.ueg.eventos.application.service.evento;

import br.ueg.eventos.application.port.in.evento.ExcluirEventoPort;
import br.ueg.eventos.application.port.out.EventoRepositoryPort;
import br.ueg.eventos.application.port.out.VinculoOrganizadorRepositoryPort;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Evento;
import br.ueg.eventos.domain.model.Perfil;
import br.ueg.eventos.domain.model.Usuario;
import br.ueg.eventos.domain.model.VinculoOrganizador;

import java.util.Optional;

/**
 * Serviço de aplicação responsável por excluir um evento.
 * Implementa validação de segurança: apenas o criador ou ADMINISTRADOR podem excluir.
 */
public class ExcluirEventoService implements ExcluirEventoPort {

    private final EventoRepositoryPort eventoRepository;
    private final VinculoOrganizadorRepositoryPort vinculoOrganizadorRepository;

    public ExcluirEventoService(EventoRepositoryPort eventoRepository,
                                VinculoOrganizadorRepositoryPort vinculoOrganizadorRepository) {
        this.eventoRepository = eventoRepository;
        this.vinculoOrganizadorRepository = vinculoOrganizadorRepository;
    }

    @Override
    public void executar(ComandoExcluirEvento comando) {
        if (comando == null) {
            throw new RegraNegocioException("O comando de exclusão de evento não pode ser nulo.");
        }

        // Validação da barreira de segurança: o usuário executor é obrigatório
        Usuario executor = comando.usuarioExecutor;
        if (executor == null) {
            throw new RegraNegocioException("Usuário executor é obrigatório para excluir o evento.");
        }

        if (comando.idEvento == null || comando.idEvento <= 0) {
            throw new RegraNegocioException("O ID do evento deve ser válido.");
        }

        // Busca o evento alvo
        Evento evento = eventoRepository.buscarPorId(comando.idEvento)
                .orElseThrow(() -> new RegraNegocioException("Evento não encontrado com o ID: " + comando.idEvento));

        if (evento.isDeletado()) {
            throw new RegraNegocioException("O evento já está excluído.");
        }

        // ── LÓGICA DE BARREIRA DE SEGURANÇA ──────────────────────────────────────────
        // 1. Se o executor for ADMINISTRADOR, a exclusão é permitida.
        // 2. Se for outro perfil, checa o VinculoOrganizador ativo.
        //    Se não encontrar ou não for o criador, bloqueia a operação com RegraNegocioException.
        if (executor.getPerfil() != Perfil.ADMINISTRADOR) {
            Optional<VinculoOrganizador> vinculoOpt = vinculoOrganizadorRepository
                    .buscarPorUsuarioEEvento(executor.getId(), comando.idEvento.intValue());

            if (vinculoOpt.isEmpty()) {
                throw new RegraNegocioException(
                        "Acesso negado: Apenas o organizador criador ou um Administrador podem excluir este evento.");
            }

            VinculoOrganizador vinculo = vinculoOpt.get();
            if (!vinculo.isCriadorPrincipal()) {
                throw new RegraNegocioException(
                        "Acesso negado: Apenas o organizador criador principal pode excluir este evento.");
            }
        }

        // Executa a exclusão lógica do evento
        eventoRepository.deletar(comando.idEvento);

        // Remove os vínculos de organizador vinculados ao evento
        vinculoOrganizadorRepository.deletarPorEvento(comando.idEvento.intValue());
    }
}
