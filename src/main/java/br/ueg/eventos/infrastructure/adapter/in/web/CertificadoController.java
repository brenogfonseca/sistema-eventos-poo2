package br.ueg.eventos.infrastructure.adapter.in.web;

import br.ueg.eventos.application.port.in.inscricao.EmitirCertificadoPort;
import br.ueg.eventos.application.port.in.pessoa.EmitirCertificadoProgramacaoPort;
import br.ueg.eventos.domain.exception.AcessoNegadoException;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

public final class CertificadoController {
    private final EmitirCertificadoPort emitir;
    private final EmitirCertificadoProgramacaoPort emitirProgramacao;
    private final EventoAuthorization autorizacao;

    public CertificadoController(EmitirCertificadoPort emitir, EventoAuthorization autorizacao) {
        this(emitir, null, autorizacao);
    }

    public CertificadoController(EmitirCertificadoPort emitir,
                                 EmitirCertificadoProgramacaoPort emitirProgramacao,
                                 EventoAuthorization autorizacao) {
        this.emitir = emitir;
        this.emitirProgramacao = emitirProgramacao;
        this.autorizacao = autorizacao;
    }

    public void registerRoutes(Javalin app) {
        app.get("/inscricoes/evento/{id}/certificado.pdf", this::emitir);
        app.get("/eventos/{idEvento}/pessoas-programacao/{idPessoa}/certificado.pdf",
                this::emitirProgramacao);
    }

    private void emitir(Context ctx) {
        try {
            long idInscricao = Long.parseLong(ctx.pathParam("id"));
            int idUsuario = autorizacao.usuarioAutenticado(ctx);
            EmitirCertificadoPort.Documento documento = emitir.executar(idInscricao, (long) idUsuario);
            ctx.status(HttpStatus.OK).contentType("application/pdf")
                    .header("Content-Disposition", "attachment; filename=\"" + documento.nomeArquivo() + "\"")
                    .result(documento.conteudo());
        } catch (AcessoNegadoException e) {
            ctx.status(HttpStatus.FORBIDDEN).result(e.getMessage());
        } catch (NumberFormatException | RegraNegocioException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result(e.getMessage());
        }
    }

    private void emitirProgramacao(Context ctx) {
        try {
            long idEvento = Long.parseLong(ctx.pathParam("idEvento"));
            long idPessoa = Long.parseLong(ctx.pathParam("idPessoa"));
            autorizacao.exigirGerenciadorDoEvento(ctx, idEvento);
            if (emitirProgramacao == null) {
                throw new IllegalStateException("Emissão de certificados da programação não configurada.");
            }
            EmitirCertificadoProgramacaoPort.Documento documento = emitirProgramacao.executar(
                    idEvento, idPessoa, ctx.queryParam("papel"));
            ctx.status(HttpStatus.OK).contentType("application/pdf")
                    .header("Content-Disposition", "attachment; filename=\"" + documento.nomeArquivo() + "\"")
                    .result(documento.conteudo());
        } catch (AcessoNegadoException e) {
            ctx.status(HttpStatus.FORBIDDEN).result(e.getMessage());
        } catch (NumberFormatException | RegraNegocioException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result(e.getMessage());
        }
    }
}
