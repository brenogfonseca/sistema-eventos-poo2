package br.ueg.eventos.infrastructure.adapter.in.web;

import br.ueg.eventos.application.port.in.frequencia.BuscarParticipantesCheckinPort;
import br.ueg.eventos.application.port.in.frequencia.RegistrarCheckinPorInscricaoPort;
import br.ueg.eventos.application.port.out.AtividadeRepositoryPort;
import br.ueg.eventos.application.port.out.UsuarioRepositoryPort;
import br.ueg.eventos.application.port.out.VinculoColaboradorRepositoryPort;
import br.ueg.eventos.application.port.out.VinculoOrganizadorRepositoryPort;
import br.ueg.eventos.application.port.out.InscricaoEventoRepositoryPort;
import br.ueg.eventos.application.port.out.InscricaoRepositoryPort;
import br.ueg.eventos.application.port.out.FrequenciaRepositoryPort;
import br.ueg.eventos.domain.exception.AcessoNegadoException;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Perfil;
import br.ueg.eventos.domain.model.Frequencia;
import br.ueg.eventos.domain.model.TipoFrequenciaEnum;
import br.ueg.eventos.infrastructure.adapter.in.web.EventoAuthorization;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.util.List;

/** Name/CPF check-in endpoints. CPF values are masked in every response. */
public final class CheckinParticipanteController {
    private final AtividadeRepositoryPort atividades;
    private final UsuarioRepositoryPort usuarios;
    private final VinculoOrganizadorRepositoryPort organizadores;
    private final VinculoColaboradorRepositoryPort colaboradores;
    private final BuscarParticipantesCheckinPort buscarPort;
    private final RegistrarCheckinPorInscricaoPort registrarPort;
    private final InscricaoRepositoryPort inscricoes;
    private final InscricaoEventoRepositoryPort inscricoesEvento;
    private final FrequenciaRepositoryPort frequencias;
    private final EventoAuthorization eventoAuthorization;

    public CheckinParticipanteController(AtividadeRepositoryPort atividades,
                                         UsuarioRepositoryPort usuarios,
                                         VinculoOrganizadorRepositoryPort organizadores,
                                         VinculoColaboradorRepositoryPort colaboradores,
                                         BuscarParticipantesCheckinPort buscarPort,
                                         RegistrarCheckinPorInscricaoPort registrarPort,
                                         InscricaoRepositoryPort inscricoes,
                                         InscricaoEventoRepositoryPort inscricoesEvento,
                                         FrequenciaRepositoryPort frequencias,
                                         EventoAuthorization eventoAuthorization) {
        this.atividades = atividades;
        this.usuarios = usuarios;
        this.organizadores = organizadores;
        this.colaboradores = colaboradores;
        this.buscarPort = buscarPort;
        this.registrarPort = registrarPort;
        this.inscricoes = inscricoes;
        this.inscricoesEvento = inscricoesEvento;
        this.frequencias = frequencias;
        this.eventoAuthorization = eventoAuthorization;
    }

    public void registerRoutes(Javalin app) {
        app.get("/atividades/{idAtividade}/participantes-checkin", this::buscarParticipantes);
        app.post("/frequencias/checkin-participante", this::registrarCheckin);
        app.get("/frequencias/atividade/{idAtividade}/participantes", this::listarParaOrganizador);
    }

    private void listarParaOrganizador(Context ctx) {
        try {
            Long idAtividade = Long.valueOf(ctx.pathParam("idAtividade"));
            eventoAuthorization.exigirGerenciadorDaAtividade(ctx, idAtividade);
            var atividade = atividades.buscarPorId(idAtividade).orElseThrow();
            var participantes = new java.util.ArrayList<java.util.Map<String, Object>>();
            for (var inscricao : inscricoes.listarPorAtividade(idAtividade)) {
                if (!inscricao.estaAtiva() || inscricao.getIdInscricaoEvento() == null) continue;
                var inscricaoEvento = inscricoesEvento.buscarPorId(inscricao.getIdInscricaoEvento()).orElse(null);
                if (inscricaoEvento == null || !inscricaoEvento.estaAtiva()) continue;
                var usuario = usuarios.buscarPorId(Math.toIntExact(inscricao.getIdUsuario())).orElse(null);
                if (usuario == null) continue;
                var item = new java.util.LinkedHashMap<String, Object>();
                item.put("idInscricaoEvento", inscricaoEvento.getId());
                item.put("nome", usuario.getNome());
                item.put("cpfMascarado", mascarar(usuario.getCpf()));
                item.put("presente", presencaConfirmada(atividade.getTipoFrequencia(),
                        frequencias.listarPorInscricao(Math.toIntExact(inscricao.getId()))));
                participantes.add(item);
            }
            ctx.json(participantes);
        } catch (AcessoNegadoException e) {
            ctx.status(HttpStatus.FORBIDDEN).result(e.getMessage());
        } catch (RegraNegocioException | NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result(e.getMessage());
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Erro interno ao listar presença dos participantes.");
        }
    }

    public static final class RegistrarRequest {
        public Long idInscricaoEvento;
        public Long idAtividade;
        public String acao;
    }

    private void buscarParticipantes(Context ctx) {
        try {
            Long idAtividade = Long.valueOf(ctx.pathParam("idAtividade"));
            exigirValidador(ctx, idAtividade);
            String termo = ctx.queryParam("q");
            List<BuscarParticipantesCheckinPort.Participante> encontrados = buscarPort.executar(idAtividade, termo);
            ctx.json(encontrados);
        } catch (AcessoNegadoException e) {
            ctx.status(HttpStatus.FORBIDDEN).result(e.getMessage());
        } catch (RegraNegocioException | NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result(e.getMessage());
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Erro interno ao buscar participantes da atividade.");
        }
    }

    private void registrarCheckin(Context ctx) {
        try {
            RegistrarRequest request = ctx.bodyAsClass(RegistrarRequest.class);
            if (request == null || request.idAtividade == null || request.idInscricaoEvento == null) {
                throw new RegraNegocioException("Selecione uma atividade e um participante.");
            }
            exigirValidador(ctx, request.idAtividade);
            var frequencia = registrarPort.executar(request.idInscricaoEvento, request.idAtividade,
                    request.acao, usuarioAutenticado(ctx));
            ctx.status(HttpStatus.CREATED).json(frequencia);
        } catch (AcessoNegadoException e) {
            ctx.status(HttpStatus.FORBIDDEN).result(e.getMessage());
        } catch (RegraNegocioException | IllegalArgumentException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result(e.getMessage());
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Erro interno ao registrar a presença.");
        }
    }

    private void exigirValidador(Context ctx, Long idAtividade) {
        var atividade = atividades.buscarPorId(idAtividade)
                .orElseThrow(() -> new RegraNegocioException("Atividade não encontrada."));
        int idUsuario = usuarioAutenticado(ctx);
        var usuario = usuarios.buscarPorId(idUsuario)
                .orElseThrow(() -> new AcessoNegadoException("Usuário autenticado não encontrado."));
        if (usuario.getPerfil() == Perfil.ADMINISTRADOR) return;
        try {
            if (organizadores.buscarPorUsuarioEEvento(idUsuario,
                    Math.toIntExact(atividade.getEventoId())).isPresent()) return;
        } catch (ArithmeticException ignored) { }
        if (colaboradores.estaVinculado(idUsuario, idAtividade)) return;
        throw new AcessoNegadoException("Você não está autorizado a validar participantes desta atividade.");
    }

    private int usuarioAutenticado(Context ctx) {
        Integer id = ctx.attribute(BearerAuthenticationFilter.ATTRIBUTE_USER_ID);
        if (id == null || id <= 0) throw new AcessoNegadoException("Sessão autenticada obrigatória.");
        return id;
    }

    private boolean presencaConfirmada(TipoFrequenciaEnum politica, List<Frequencia> marcacoes) {
        var correcao = marcacoes.stream().filter(f -> "CORRECAO_ORGANIZADOR".equalsIgnoreCase(f.getOrigem()))
                .max(java.util.Comparator.comparing(Frequencia::getDataHora)
                        .thenComparing(f -> f.getId() == null ? 0 : f.getId()));
        if (correcao.isPresent()) return correcao.get().isPresente();
        if (politica == TipoFrequenciaEnum.CHECKIN) {
            return marcacoes.stream().anyMatch(f -> f.isPresente() && "CHECKIN".equalsIgnoreCase(f.getTipo()));
        }
        if (politica == TipoFrequenciaEnum.ENTRADA_SAIDA) {
            boolean entrada = marcacoes.stream().anyMatch(f -> f.isPresente()
                    && java.util.List.of("QR_ENTRADA", "BUSCA_ENTRADA").contains(f.getOrigem().toUpperCase()));
            boolean saida = marcacoes.stream().anyMatch(f -> f.isPresente()
                    && java.util.List.of("QR_SAIDA", "BUSCA_SAIDA").contains(f.getOrigem().toUpperCase()));
            return entrada && saida;
        }
        return marcacoes.stream().filter(f -> "MANUAL".equalsIgnoreCase(f.getTipo()))
                .max(java.util.Comparator.comparing(Frequencia::getDataHora)
                        .thenComparing(f -> f.getId() == null ? 0 : f.getId()))
                .map(Frequencia::isPresente).orElse(false);
    }

    private String mascarar(String cpf) {
        return cpf == null || cpf.length() != 11 ? "CPF não cadastrado" : "***.***.***-" + cpf.substring(9);
    }
}
