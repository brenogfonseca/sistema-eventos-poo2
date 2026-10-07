package br.ueg.eventos.application.service.evento;

import br.ueg.eventos.application.port.in.evento.AlterarEventoPort;
import br.ueg.eventos.application.port.out.EventoRepositoryPort;
import br.ueg.eventos.application.port.out.VinculoOrganizadorRepositoryPort;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Evento;
import br.ueg.eventos.domain.model.Perfil;
import br.ueg.eventos.domain.model.Periodo;
import br.ueg.eventos.domain.model.Usuario;
import br.ueg.eventos.domain.model.VinculoOrganizador;

import java.util.List;
import java.util.Optional;

/**
 * Serviço de aplicação responsável por alterar os dados de um evento.
 * Implementa validação de segurança e proteção de invariantes de negócio.
 */
public class AlterarEventoService implements AlterarEventoPort {

    private final EventoRepositoryPort eventoRepository;
    private final VinculoOrganizadorRepositoryPort vinculoOrganizadorRepository;

    public AlterarEventoService(EventoRepositoryPort eventoRepository,
                                VinculoOrganizadorRepositoryPort vinculoOrganizadorRepository) {
        this.eventoRepository = eventoRepository;
        this.vinculoOrganizadorRepository = vinculoOrganizadorRepository;
    }

    @Override
    public Evento executar(ComandoAlterarEvento comando) {
        if (comando == null) {
            throw new RegraNegocioException("O comando de alteração de evento não pode ser nulo.");
        }

        // Validação da barreira de segurança: o usuário executor é obrigatório
        Usuario executor = comando.usuarioExecutor;
        if (executor == null) {
            throw new RegraNegocioException("Usuário executor é obrigatório para alterar o evento.");
        }

        if (comando.idEvento == null || comando.idEvento <= 0) {
            throw new RegraNegocioException("O ID do evento deve ser válido.");
        }

        // Busca o evento alvo
        Evento evento = eventoRepository.buscarPorId(comando.idEvento)
                .orElseThrow(() -> new RegraNegocioException("Evento não encontrado com o ID: " + comando.idEvento));

        if (evento.isDeletado()) {
            throw new RegraNegocioException("Não é possível alterar um evento excluído.");
        }

        // ── LÓGICA DE BARREIRA DE SEGURANÇA ──────────────────────────────────────────
        // 1. Se o executor for ADMINISTRADOR, a ação é irrestritamente permitida.
        // 2. Caso contrário, o sistema consulta se existe um VinculoOrganizador para
        //    aquele usuarioId e eventoId. Se não encontrar ou não for o criador, bloqueia a operação.
        if (executor.getPerfil() != Perfil.ADMINISTRADOR) {
            Optional<VinculoOrganizador> vinculoOpt = vinculoOrganizadorRepository
                    .buscarPorUsuarioEEvento(executor.getId(), comando.idEvento.intValue());

            if (vinculoOpt.isEmpty()) {
                throw new RegraNegocioException(
                        "Acesso negado: Apenas o organizador criador ou um Administrador podem alterar este evento.");
            }

            VinculoOrganizador vinculo = vinculoOpt.get();
            if (!vinculo.isCriadorPrincipal()) {
                throw new RegraNegocioException(
                        "Acesso negado: Apenas o organizador criador principal pode alterar este evento.");
            }
        }

        // Validação de período e conflito com outros eventos
        Periodo novoPeriodo = new Periodo(comando.inicio, comando.fim);
        List<Evento> eventosExistentes = eventoRepository.listarTodos();
        for (Evento e : eventosExistentes) {
            // Ignora o próprio evento e eventos deletados ao verificar conflitos de agenda
            if (!e.getId().equals(comando.idEvento) && !e.isDeletado() && e.getPeriodo().conflitaCom(novoPeriodo)) {
                throw new RegraNegocioException("Já existe outro evento marcado para este período.");
            }
        }

        // Atualiza os dados respeitando o encapsulamento e proteção de invariantes do domínio
        evento.atualizarDados(comando.titulo, comando.descricao, novoPeriodo, comando.capacidade);

        return eventoRepository.salvar(evento);
    }
}
