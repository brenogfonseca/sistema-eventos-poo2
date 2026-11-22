package br.ueg.eventos.infrastructure.adapter.in.web;

import br.ueg.eventos.application.port.in.pessoa.VincularPessoaProgramacaoPort;
import br.ueg.eventos.domain.exception.AcessoNegadoException;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.PessoaProgramacao;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

public final class PessoaProgramacaoController {
    private final VincularPessoaProgramacaoPort pessoas;
    private final EventoAuthorization autorizacao;

    public PessoaProgramacaoController(VincularPessoaProgramacaoPort pessoas, EventoAuthorization autorizacao) {
        this.pessoas = pessoas;
        this.autorizacao = autorizacao;
    }

    public PessoaProgramacaoController(VincularPessoaProgramacaoPort pessoas) {
        this(pessoas, null);
    }

    public void registerRoutes(Javalin app) {
        app.post("/atividades/{id}/pessoas", this::vincular);
        app.get("/atividades/{id}/pessoas", this::listar);
        app.get("/eventos/{id}/pessoas-programacao", this::listarPorEvento);
    }

    public static final class PessoaRequest {
        public String nome;
        public String organizacao;
        public String biografia;
        public String fotoUrl;
        public String papel;
    }

    private void vincular(Context ctx) {
        try {
            PessoaRequest request = ctx.bodyAsClass(PessoaRequest.class);
            PessoaProgramacao pessoa = new PessoaProgramacao(request.nome, request.organizacao,
                    request.biografia, request.fotoUrl);
            int idUsuario = Integer.parseInt(ctx.header("X-Usuario-Id"));
            var criado = pessoas.executar(Long.valueOf(ctx.pathParam("id")), pessoa, request.papel, idUsuario);
            ctx.status(HttpStatus.CREATED).json(criado);
        } catch (AcessoNegadoException e) {
            ctx.status(HttpStatus.FORBIDDEN).result(e.getMessage());
        } catch (RegraNegocioException | NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result(e.getMessage());
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Não foi possível vincular a pessoa à atividade.");
        }
    }

    private void listar(Context ctx) {
        try {
            Long idAtividade = Long.valueOf(ctx.pathParam("id"));
            if (autorizacao != null) autorizacao.exigirLeituraDaAtividade(ctx, idAtividade);
            ctx.status(HttpStatus.OK).json(pessoas.listar(idAtividade));
        } catch (AcessoNegadoException e) {
            ctx.status(HttpStatus.FORBIDDEN).result(e.getMessage());
        } catch (RegraNegocioException | NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result(e.getMessage());
        }
    }

    private void listarPorEvento(Context ctx) {
        try {
            Long idEvento = Long.valueOf(ctx.pathParam("id"));
            if (autorizacao != null) autorizacao.exigirLeituraDaProgramacao(ctx, idEvento);
            ctx.status(HttpStatus.OK).json(pessoas.listarPorEvento(idEvento));
        } catch (AcessoNegadoException e) {
            ctx.status(HttpStatus.FORBIDDEN).result(e.getMessage());
        } catch (RegraNegocioException | NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result(e.getMessage());
        }
    }
}
