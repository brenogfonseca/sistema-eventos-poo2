package br.ueg.eventos.application.service.frequencia;

import br.ueg.eventos.application.port.in.frequencia.BuscarParticipantesCheckinPort;
import br.ueg.eventos.application.port.out.AtividadeRepositoryPort;
import br.ueg.eventos.application.port.out.InscricaoEventoRepositoryPort;
import br.ueg.eventos.application.port.out.InscricaoRepositoryPort;
import br.ueg.eventos.application.port.out.UsuarioRepositoryPort;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.MetodoIdentificacaoCheckinEnum;
import br.ueg.eventos.domain.model.Usuario;

import java.text.Normalizer;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;

/** Returns only active registrants who selected the activity, with CPF masked. */
public final class BuscarParticipantesCheckinService implements BuscarParticipantesCheckinPort {
    private static final int LIMITE_RESULTADOS = 25;
    private final AtividadeRepositoryPort atividades;
    private final InscricaoRepositoryPort inscricoes;
    private final InscricaoEventoRepositoryPort inscricoesEvento;
    private final UsuarioRepositoryPort usuarios;

    public BuscarParticipantesCheckinService(AtividadeRepositoryPort atividades,
                                             InscricaoRepositoryPort inscricoes,
                                             InscricaoEventoRepositoryPort inscricoesEvento,
                                             UsuarioRepositoryPort usuarios) {
        this.atividades = atividades;
        this.inscricoes = inscricoes;
        this.inscricoesEvento = inscricoesEvento;
        this.usuarios = usuarios;
    }

    @Override
    public List<Participante> executar(Long idAtividade, String termo) {
        var atividade = atividades.buscarPorId(idAtividade)
                .orElseThrow(() -> new RegraNegocioException("Atividade não encontrada."));
        if (atividade.getMetodoIdentificacaoCheckin() != MetodoIdentificacaoCheckinEnum.NOME_CPF) {
            throw new RegraNegocioException("A atividade está configurada para identificação por QR Code.");
        }
        String busca = normalizarNome(termo);
        String cpfBusca = termo == null ? "" : termo.replaceAll("\\D", "");
        if (busca.length() < 2 && cpfBusca.length() < 4) return List.of();

        List<Participante> encontrados = new ArrayList<>();
        for (var inscricao : inscricoes.listarPorAtividade(idAtividade)) {
            if (!inscricao.estaAtiva() || inscricao.getIdInscricaoEvento() == null) continue;
            var inscricaoEvento = inscricoesEvento.buscarPorId(inscricao.getIdInscricaoEvento()).orElse(null);
            if (inscricaoEvento == null || !inscricaoEvento.estaAtiva()) continue;
            Usuario usuario = usuarios.buscarPorId(Math.toIntExact(inscricao.getIdUsuario())).orElse(null);
            if (usuario == null || !corresponde(usuario, busca, cpfBusca)) continue;
            encontrados.add(new Participante(inscricaoEvento.getId(), usuario.getNome(), mascarar(usuario.getCpf())));
            if (encontrados.size() >= LIMITE_RESULTADOS) break;
        }
        return List.copyOf(encontrados);
    }

    private boolean corresponde(Usuario usuario, String busca, String cpfBusca) {
        if (!busca.isBlank() && normalizarNome(usuario.getNome()).contains(busca)) return true;
        return !cpfBusca.isBlank() && usuario.getCpf() != null && usuario.getCpf().contains(cpfBusca);
    }

    private String normalizarNome(String valor) {
        if (valor == null) return "";
        return Normalizer.normalize(valor.trim().toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
    }

    private String mascarar(String cpf) {
        return cpf == null || cpf.length() != 11 ? "CPF não cadastrado" : "***.***.***-" + cpf.substring(9);
    }
}
