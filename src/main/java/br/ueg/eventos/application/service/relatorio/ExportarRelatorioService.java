package br.ueg.eventos.application.service.relatorio;

import br.ueg.eventos.application.port.in.relatorio.ExportarRelatorioPort;
import br.ueg.eventos.application.port.out.EventoRepositoryPort;
import br.ueg.eventos.application.port.out.RelatorioRepositoryPort;
import br.ueg.eventos.application.port.out.UsuarioRepositoryPort;
import br.ueg.eventos.application.port.out.VinculoOrganizadorRepositoryPort;
import br.ueg.eventos.domain.exception.AcessoNegadoException;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Perfil;

public final class ExportarRelatorioService implements ExportarRelatorioPort {
    private final EventoRepositoryPort eventos;
    private final UsuarioRepositoryPort usuarios;
    private final VinculoOrganizadorRepositoryPort organizadores;
    private final RelatorioRepositoryPort relatorios;

    public ExportarRelatorioService(EventoRepositoryPort eventos,
                                    UsuarioRepositoryPort usuarios,
                                    VinculoOrganizadorRepositoryPort organizadores,
                                    RelatorioRepositoryPort relatorios) {
        this.eventos = eventos;
        this.usuarios = usuarios;
        this.organizadores = organizadores;
        this.relatorios = relatorios;
    }

    @Override
    public String inscricoesCsv(Long idEvento, Integer idExecutor) {
        autorizar(idEvento, idExecutor);
        return relatorios.exportarInscricoesCsv(idEvento);
    }

    @Override
    public String frequenciasCsv(Long idEvento, Integer idExecutor) {
        autorizar(idEvento, idExecutor);
        return relatorios.exportarFrequenciasCsv(idEvento);
    }

    private void autorizar(Long idEvento, Integer idExecutor) {
        if (idEvento == null || idEvento <= 0 || idExecutor == null || idExecutor <= 0) {
            throw new RegraNegocioException("Evento e usuário autenticado devem ser válidos.");
        }
        eventos.buscarPorId(idEvento).orElseThrow(() -> new RegraNegocioException("Evento não encontrado."));
        var usuario = usuarios.buscarPorId(idExecutor)
                .orElseThrow(() -> new AcessoNegadoException("Usuário autenticado não encontrado."));
        if (usuario.getPerfil() == Perfil.ADMINISTRADOR) return;
        try {
            if (organizadores.buscarPorUsuarioEEvento(idExecutor, Math.toIntExact(idEvento)).isPresent()) return;
        } catch (ArithmeticException ignored) {
            // IDs fora do limite do vínculo não autorizam o acesso.
        }
        throw new AcessoNegadoException("Somente organizadores do evento podem exportar estes dados.");
    }
}
