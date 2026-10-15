package br.ueg.eventos.infrastructure.adapter.in.web;

import br.ueg.eventos.application.port.in.frequencia.GerarCheckinQrPort;
import br.ueg.eventos.application.port.in.frequencia.RegistrarCheckinQrPort;
import br.ueg.eventos.domain.exception.AcessoNegadoException;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Frequencia;
import br.ueg.eventos.infrastructure.adapter.out.qrcode.ZxingQrCodeGenerator;
import com.google.zxing.WriterException;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.io.IOException;
import java.util.Base64;

public class CheckinQrController {

    private static final String HEADER_USUARIO_ID = "X-Usuario-Id";

    private final GerarCheckinQrPort gerarCheckinQrPort;
    private final RegistrarCheckinQrPort registrarCheckinQrPort;
    private final ZxingQrCodeGenerator qrCodeGenerator;

    public CheckinQrController(
            GerarCheckinQrPort gerarCheckinQrPort,
            RegistrarCheckinQrPort registrarCheckinQrPort,
            ZxingQrCodeGenerator qrCodeGenerator
    ) {
        this.gerarCheckinQrPort = gerarCheckinQrPort;
        this.registrarCheckinQrPort = registrarCheckinQrPort;
        this.qrCodeGenerator = qrCodeGenerator;
    }

    public void registerRoutes(Javalin app) {
        app.get("/inscricoes/{id}/checkin-qr", this::gerarQrCode);
        app.post("/frequencias/checkin-qr", this::registrarCheckin);
    }

    public static class RegistrarCheckinRequest {
        public String token;
    }

    public static class CheckinQrResponse {
        public final String token;
        public final String expiraEm;
        public final String imagemBase64;

        public CheckinQrResponse(String token, String expiraEm, String imagemBase64) {
            this.token = token;
            this.expiraEm = expiraEm;
            this.imagemBase64 = imagemBase64;
        }
    }

    private void gerarQrCode(Context ctx) {
        try {
            Long idInscricao = Long.valueOf(ctx.pathParam("id"));
            Integer idUsuario = extrairUsuarioId(ctx);
            GerarCheckinQrPort.DadosCheckinQr dados =
                    gerarCheckinQrPort.executar(idInscricao, idUsuario);
            byte[] png = qrCodeGenerator.gerarPng(dados.getToken());

            ctx.status(HttpStatus.OK).json(new CheckinQrResponse(
                    dados.getToken(),
                    dados.getExpiraEm().toString(),
                    Base64.getEncoder().encodeToString(png)
            ));
        } catch (AcessoNegadoException e) {
            ctx.status(HttpStatus.FORBIDDEN).result(e.getMessage());
        } catch (RegraNegocioException | NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result(e.getMessage());
        } catch (IllegalStateException e) {
            ctx.status(HttpStatus.SERVICE_UNAVAILABLE).result(e.getMessage());
        } catch (WriterException | IOException e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .result("Não foi possível gerar a imagem do QR Code.");
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .result("Erro interno ao gerar o QR Code.");
        }
    }

    private void registrarCheckin(Context ctx) {
        try {
            Integer idResponsavel = extrairUsuarioId(ctx);
            RegistrarCheckinRequest request;
            try {
                request = ctx.bodyAsClass(RegistrarCheckinRequest.class);
            } catch (Exception e) {
                ctx.status(HttpStatus.BAD_REQUEST)
                        .result("Envie um JSON válido com o campo token.");
                return;
            }
            Frequencia frequencia = registrarCheckinQrPort.executar(
                    request == null ? null : request.token,
                    idResponsavel
            );
            ctx.status(HttpStatus.CREATED).json(frequencia);
        } catch (AcessoNegadoException e) {
            ctx.status(HttpStatus.FORBIDDEN).result(e.getMessage());
        } catch (RegraNegocioException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result(e.getMessage());
        } catch (IllegalStateException e) {
            ctx.status(HttpStatus.SERVICE_UNAVAILABLE).result(e.getMessage());
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .result("Erro interno ao registrar o check-in.");
        }
    }

    private Integer extrairUsuarioId(Context ctx) {
        String valor = ctx.header(HEADER_USUARIO_ID);
        if (valor == null || valor.isBlank()) {
            throw new AcessoNegadoException(
                    "Informe o usuário autenticado no cabeçalho X-Usuario-Id."
            );
        }
        try {
            Integer id = Integer.valueOf(valor.trim());
            if (id <= 0) {
                throw new NumberFormatException();
            }
            return id;
        } catch (NumberFormatException e) {
            throw new AcessoNegadoException("O usuário autenticado informado é inválido.");
        }
    }
}
