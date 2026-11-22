package br.ueg.eventos.infrastructure.adapter.in.web;

import br.ueg.eventos.application.port.out.AtividadeRepositoryPort;
import br.ueg.eventos.application.port.out.EventoRepositoryPort;
import br.ueg.eventos.application.port.out.UsuarioRepositoryPort;
import br.ueg.eventos.application.port.out.VinculoColaboradorRepositoryPort;
import br.ueg.eventos.application.port.out.VinculoOrganizadorRepositoryPort;
import br.ueg.eventos.domain.exception.AcessoNegadoException;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Perfil;
import br.ueg.eventos.infrastructure.adapter.in.web.EventoAuthorization;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Manages collaborators per activity and provides each operator their allowed activities. */
public final class VinculoColaboradorController {
    private final AtividadeRepositoryPort atividades;
    private final EventoRepositoryPort eventos;
    private final UsuarioRepositoryPort usuarios;
    private final VinculoOrganizadorRepositoryPort organizadores;
    private final VinculoColaboradorRepositoryPort colaboradores;
    private final EventoAuthorization autorizacao;

    public VinculoColaboradorController(AtividadeRepositoryPort atividades,
                                        EventoRepositoryPort eventos,
                                        UsuarioRepositoryPort usuarios,
                                        VinculoOrganizadorRepositoryPort organizadores,
                                        VinculoColaboradorRepositoryPort colaboradores,
                                        EventoAuthorization autorizacao) {
        this.atividades = atividades;
        this.eventos = eventos;
        this.usuarios = usuarios;
        this.organizadores = organizadores;
        this.colaboradores = colaboradores;
        this.autorizacao = autorizacao;
    }

    public void registerRoutes(Javalin app) {
        app.post("/atividades/{idAtividade}/colaboradores", this::vincular);
        app.get("/atividades/{idAtividade}/colaboradores", this::listar);
        app.delete("/atividades/{idAtividade}/colaboradores/{idUsuario}", this::remover);
        app.get("/frequencias/atividades-validaveis", this::listarAtividadesValidaveis);
    }

    public static final class VincularRequest { public String email; }

    private void vincular(Context ctx) {
        try {
            Long idAtividade = Long.valueOf(ctx.pathParam("idAtividade"));
            autorizacao.exigirGerenciadorDaAtividade(ctx, idAtividade);
            VincularRequest request = ctx.bodyAsClass(VincularRequest.class);
            if (request.email == null || request.email.isBlank()) {
                throw new RegraNegocioException("Informe o e-mail da conta que será colaboradora.");
            }
            var usuario = usuarios.buscarPorEmail(request.email.trim())
                    .orElseThrow(() -> new RegraNegocioException("Não há conta cadastrada com esse e-mail."));
            if (usuario.getPerfil() == Perfil.ADMINISTRADOR) {
                throw new RegraNegocioException("Administradores não precisam ser vinculados como colaboradores.");
            }
            colaboradores.vincular(usuario.getId(), idAtividade);
            ctx.status(HttpStatus.CREATED).json(Map.of(
                    "idUsuario", usuario.getId(), "nome", usuario.getNome(), "email", usuario.getEmail()));
        } catch (AcessoNegadoException e) {
            ctx.status(HttpStatus.FORBIDDEN).result(e.getMessage());
        } catch (RegraNegocioException | NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result(e.getMessage());
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Erro interno ao vincular colaborador.");
        }
    }

    private void listar(Context ctx) {
        try {
            Long idAtividade = Long.valueOf(ctx.pathParam("idAtividade"));
            autorizacao.exigirGerenciadorDaAtividade(ctx, idAtividade);
            List<Map<String, Object>> resposta = new ArrayList<>();
            for (Integer idUsuario : colaboradores.listarUsuariosPorAtividade(idAtividade)) {
                usuarios.buscarPorId(idUsuario).ifPresent(usuario -> resposta.add(Map.of(
                        "idUsuario", usuario.getId(), "nome", usuario.getNome(), "email", usuario.getEmail())));
            }
            ctx.json(resposta);
        } catch (AcessoNegadoException e) {
            ctx.status(HttpStatus.FORBIDDEN).result(e.getMessage());
        } catch (RegraNegocioException | NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result(e.getMessage());
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Erro interno ao listar colaboradores.");
        }
    }

    private void remover(Context ctx) {
        try {
            Long idAtividade = Long.valueOf(ctx.pathParam("idAtividade"));
            Integer idUsuario = Integer.valueOf(ctx.pathParam("idUsuario"));
            autorizacao.exigirGerenciadorDaAtividade(ctx, idAtividade);
            colaboradores.remover(idUsuario, idAtividade);
            ctx.status(HttpStatus.NO_CONTENT);
        } catch (AcessoNegadoException e) {
            ctx.status(HttpStatus.FORBIDDEN).result(e.getMessage());
        } catch (RegraNegocioException | NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result(e.getMessage());
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Erro interno ao remover colaborador.");
        }
    }

    private void listarAtividadesValidaveis(Context ctx) {
        try {
            int idUsuario = autorizacao.usuarioAutenticado(ctx);
            boolean administrador = autorizacao.ehAdministrador(ctx);
            List<Map<String, Object>> resposta = new ArrayList<>();
            List<Long> vinculadas = colaboradores.listarAtividadesPorUsuario(idUsuario);
            for (var evento : eventos.listarTodos()) {
                if (evento.isDeletado()) continue;
                boolean organizador = administrador;
                if (!organizador) {
                    try {
                        organizador = organizadores.buscarPorUsuarioEEvento(idUsuario,
                                Math.toIntExact(evento.getId())).isPresent();
                    } catch (ArithmeticException ignored) { }
                }
                for (var atividade : atividades.listarPorEvento(evento.getId())) {
                    if (!organizador && !vinculadas.contains(atividade.getId())) continue;
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("idEvento", evento.getId());
                    item.put("tituloEvento", evento.getTitulo());
                    item.put("idAtividade", atividade.getId());
                    item.put("nomeAtividade", atividade.getNome());
                    item.put("data", atividade.getData());
                    item.put("horaInicio", atividade.getHoraInicio());
                    item.put("horaFim", atividade.getHoraFim());
                    item.put("tipoFrequencia", atividade.getTipoFrequencia());
                    item.put("metodoIdentificacaoCheckin", atividade.getMetodoIdentificacaoCheckin());
                    resposta.add(item);
                }
            }
            ctx.json(resposta);
        } catch (AcessoNegadoException e) {
            ctx.status(HttpStatus.FORBIDDEN).result(e.getMessage());
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Erro interno ao consultar atividades autorizadas.");
        }
    }
}
