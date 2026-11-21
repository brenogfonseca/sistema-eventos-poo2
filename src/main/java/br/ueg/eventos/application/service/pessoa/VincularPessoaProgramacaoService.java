package br.ueg.eventos.application.service.pessoa;

import br.ueg.eventos.application.port.in.pessoa.VincularPessoaProgramacaoPort;
import br.ueg.eventos.application.port.out.AtividadeRepositoryPort;
import br.ueg.eventos.application.port.out.EventoRepositoryPort;
import br.ueg.eventos.application.port.out.PessoaProgramacaoRepositoryPort;
import br.ueg.eventos.application.port.out.UsuarioRepositoryPort;
import br.ueg.eventos.application.port.out.VinculoOrganizadorRepositoryPort;
import br.ueg.eventos.domain.exception.AcessoNegadoException;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Perfil;
import br.ueg.eventos.domain.model.PessoaProgramacao;
import java.util.List;
import java.util.Set;

public final class VincularPessoaProgramacaoService implements VincularPessoaProgramacaoPort {
    private static final Set<String> PAPEIS = Set.of("PALESTRANTE", "APRESENTADOR", "RESPONSAVEL");
    private final AtividadeRepositoryPort atividades;
    private final EventoRepositoryPort eventos;
    private final UsuarioRepositoryPort usuarios;
    private final VinculoOrganizadorRepositoryPort organizadores;
    private final PessoaProgramacaoRepositoryPort pessoas;

    public VincularPessoaProgramacaoService(AtividadeRepositoryPort atividades,
                                            EventoRepositoryPort eventos,
                                            UsuarioRepositoryPort usuarios,
                                            VinculoOrganizadorRepositoryPort organizadores,
                                            PessoaProgramacaoRepositoryPort pessoas) {
        this.atividades = atividades;
        this.eventos = eventos;
        this.usuarios = usuarios;
        this.organizadores = organizadores;
        this.pessoas = pessoas;
    }

    @Override
    public PessoaProgramacao executar(Long idAtividade, PessoaProgramacao pessoa, String papel, Integer idExecutor) {
        if (idAtividade == null || idAtividade <= 0 || pessoa == null) {
            throw new RegraNegocioException("Atividade e pessoa são obrigatórias.");
        }
        String papelNormalizado = papel == null ? "" : papel.trim().toUpperCase();
        if (!PAPEIS.contains(papelNormalizado)) throw new RegraNegocioException("Papel inválido para a programação.");
        var atividade = atividades.buscarPorId(idAtividade)
                .orElseThrow(() -> new RegraNegocioException("Atividade não encontrada."));
        var executor = usuarios.buscarPorId(idExecutor)
                .orElseThrow(() -> new AcessoNegadoException("Usuário autenticado não encontrado."));
        boolean autorizado = executor.getPerfil() == Perfil.ADMINISTRADOR
                || organizadores.buscarPorUsuarioEEvento(idExecutor, Math.toIntExact(atividade.getEventoId())).isPresent();
        if (!autorizado) throw new AcessoNegadoException("Somente o organizador do evento pode alterar a programação.");
        return pessoas.vincular(idAtividade, pessoa, papelNormalizado);
    }

    @Override
    public List<PessoaProgramacaoRepositoryPort.VinculoPessoa> listar(Long idAtividade) {
        if (idAtividade == null || idAtividade <= 0) throw new RegraNegocioException("Atividade inválida.");
        var atividade = atividades.buscarPorId(idAtividade)
                .orElseThrow(() -> new RegraNegocioException("Atividade não encontrada."));
        var evento = eventos.buscarPorId(atividade.getEventoId())
                .orElseThrow(() -> new RegraNegocioException("Atividade não encontrada."));
        if (evento.isDeletado()) {
            throw new RegraNegocioException("Atividade não encontrada.");
        }
        return pessoas.listarPorAtividade(idAtividade);
    }

    @Override
    public List<PessoaProgramacaoRepositoryPort.VinculoPessoa> listarPorEvento(Long idEvento) {
        if (idEvento == null || idEvento <= 0) throw new RegraNegocioException("Evento inválido.");
        var evento = eventos.buscarPorId(idEvento)
                .orElseThrow(() -> new RegraNegocioException("Evento não encontrado."));
        if (evento.isDeletado()) {
            throw new RegraNegocioException("Evento não encontrado.");
        }
        return pessoas.listarPorEvento(idEvento);
    }
}
