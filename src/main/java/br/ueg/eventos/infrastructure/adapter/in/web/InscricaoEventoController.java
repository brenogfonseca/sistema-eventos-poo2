package br.ueg.eventos.infrastructure.adapter.in.web;

import br.ueg.eventos.application.port.in.inscricao.InscreverNoEventoPort;
import br.ueg.eventos.application.port.in.inscricao.CalcularFrequenciaCertificadoPort;
import br.ueg.eventos.application.port.in.inscricao.CancelarInscricaoEventoPort;
import br.ueg.eventos.application.port.in.inscricao.ListarAtividadesEscolhidasPort;
import br.ueg.eventos.application.port.in.inscricao.ListarInscricoesEventoPorUsuarioPort;
import br.ueg.eventos.application.port.in.inscricao.SelecionarAtividadesPort;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import java.util.List;

public class InscricaoEventoController {
    private final InscreverNoEventoPort inscreverPort;
    private final ListarInscricoesEventoPorUsuarioPort listarPort;
    private final SelecionarAtividadesPort selecionarPort;
    private final ListarAtividadesEscolhidasPort listarAtividadesPort;
    private final CalcularFrequenciaCertificadoPort calcularFrequenciaPort;
    private final CancelarInscricaoEventoPort cancelarPort;

    public InscricaoEventoController(InscreverNoEventoPort inscreverPort,
                                    ListarInscricoesEventoPorUsuarioPort listarPort,
                                    SelecionarAtividadesPort selecionarPort,
                                    ListarAtividadesEscolhidasPort listarAtividadesPort,
                                    CalcularFrequenciaCertificadoPort calcularFrequenciaPort,
                                    CancelarInscricaoEventoPort cancelarPort) {
        this.inscreverPort = inscreverPort;
        this.listarPort = listarPort;
        this.selecionarPort = selecionarPort;
        this.listarAtividadesPort = listarAtividadesPort;
        this.calcularFrequenciaPort = calcularFrequenciaPort;
        this.cancelarPort = cancelarPort;
    }

    public void registerRoutes(Javalin app) {
        app.post("/inscricoes/evento", this::inscreverNoEvento);
        app.get("/inscricoes/eventos/usuario/{idUsuario}", this::listarPorUsuario);
        app.put("/inscricoes/evento/{id}/atividades", this::selecionarAtividades);
        app.get("/inscricoes/evento/{id}/atividades", this::listarAtividades);
        app.get("/inscricoes/evento/{id}/frequencia-certificado", this::calcularFrequencia);
        app.delete("/inscricoes/evento/{id}", this::cancelar);
    }

    public static class InscreverRequest { public Long idEvento; }
    public static class SelecionarRequest { public List<Long> idsAtividades; }

    private void inscreverNoEvento(Context ctx) {
        try {
            InscreverRequest request = ctx.bodyAsClass(InscreverRequest.class);
            ctx.status(HttpStatus.CREATED).json(inscreverPort.executar(request.idEvento, idUsuario(ctx)));
        } catch (RegraNegocioException e) { ctx.status(HttpStatus.BAD_REQUEST).result(e.getMessage()); }
        catch (Exception e) { ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Erro interno ao inscrever no evento."); }
    }

    private void listarPorUsuario(Context ctx) {
        try {
            long idUsuario = Long.parseLong(ctx.pathParam("idUsuario"));
            if (idUsuario != idUsuario(ctx)) { ctx.status(HttpStatus.FORBIDDEN).result("Só é permitido consultar as próprias inscrições."); return; }
            ctx.status(HttpStatus.OK).json(listarPort.executar(idUsuario));
        } catch (RegraNegocioException | NumberFormatException e) { ctx.status(HttpStatus.BAD_REQUEST).result(e.getMessage()); }
        catch (Exception e) { ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Erro interno ao listar inscrições."); }
    }

    private void selecionarAtividades(Context ctx) {
        try {
            long id = Long.parseLong(ctx.pathParam("id"));
            SelecionarRequest request = ctx.bodyAsClass(SelecionarRequest.class);
            ctx.status(HttpStatus.OK).json(selecionarPort.executar(id, (long) idUsuario(ctx), request.idsAtividades));
        } catch (RegraNegocioException | NumberFormatException e) { ctx.status(HttpStatus.BAD_REQUEST).result(e.getMessage()); }
        catch (Exception e) { ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Erro interno ao escolher atividades."); }
    }

    private void listarAtividades(Context ctx) {
        try {
            long id = Long.parseLong(ctx.pathParam("id"));
            ctx.status(HttpStatus.OK).json(listarAtividadesPort.executar(id, (long) idUsuario(ctx)));
        } catch (RegraNegocioException | NumberFormatException e) { ctx.status(HttpStatus.BAD_REQUEST).result(e.getMessage()); }
        catch (Exception e) { ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Erro interno ao listar atividades escolhidas."); }
    }

    private void calcularFrequencia(Context ctx) {
        try {
            long id = Long.parseLong(ctx.pathParam("id"));
            ctx.status(HttpStatus.OK).json(calcularFrequenciaPort.executar(id, (long) idUsuario(ctx)));
        } catch (RegraNegocioException | NumberFormatException e) { ctx.status(HttpStatus.BAD_REQUEST).result(e.getMessage()); }
        catch (Exception e) { ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Erro interno ao calcular frequência."); }
    }

    private void cancelar(Context ctx) {
        try {
            long id = Long.parseLong(ctx.pathParam("id"));
            cancelarPort.executar(id, (long) idUsuario(ctx));
            ctx.status(HttpStatus.NO_CONTENT);
        } catch (RegraNegocioException | NumberFormatException e) { ctx.status(HttpStatus.BAD_REQUEST).result(e.getMessage()); }
        catch (Exception e) { ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Erro interno ao cancelar inscrição."); }
    }

    private Integer idUsuario(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.header("X-Usuario-Id"));
            if (id <= 0) throw new NumberFormatException();
            return id;
        } catch (Exception e) { throw new RegraNegocioException("Usuário autenticado obrigatório no cabeçalho X-Usuario-Id."); }
    }
}
