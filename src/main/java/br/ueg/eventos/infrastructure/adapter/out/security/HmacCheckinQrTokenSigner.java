package br.ueg.eventos.infrastructure.adapter.out.security;

import br.ueg.eventos.application.port.out.CheckinQrTokenSignerPort;
import br.ueg.eventos.domain.exception.RegraNegocioException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

/** Implementação HMAC-SHA256 dos tokens de check-in. */
public class HmacCheckinQrTokenSigner implements CheckinQrTokenSignerPort {

    private static final String ALGORITMO = "HmacSHA256";
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

    private final String segredo;

    public HmacCheckinQrTokenSigner() {
        this(System.getenv("EVENTOS_QR_SECRET"));
    }

    HmacCheckinQrTokenSigner(String segredo) {
        this.segredo = segredo;
    }

    @Override
    public String emitir(Long idInscricao, Instant expiraEm) {
        if (idInscricao == null || idInscricao <= 0 || expiraEm == null) {
            throw new IllegalArgumentException("Inscrição e expiração são obrigatórias para emitir o QR Code.");
        }

        String payload = "v1|" + idInscricao + "|" + expiraEm.getEpochSecond()
                + "|" + UUID.randomUUID().toString().replace("-", "");
        String payloadCodificado = ENCODER.encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        String assinatura = ENCODER.encodeToString(calcularHmac(payloadCodificado));
        return "v1." + payloadCodificado + "." + assinatura;
    }

    @Override
    public DadosToken validar(String token) {
        try {
            if (token == null || token.length() > 2048) {
                throw new IllegalArgumentException();
            }

            String[] partes = token.split("\\.", -1);
            if (partes.length != 3 || !"v1".equals(partes[0])) {
                throw new IllegalArgumentException();
            }

            byte[] assinaturaRecebida = DECODER.decode(partes[2]);
            byte[] assinaturaEsperada = calcularHmac(partes[1]);
            if (!MessageDigest.isEqual(assinaturaEsperada, assinaturaRecebida)) {
                throw new IllegalArgumentException();
            }

            String payload = new String(DECODER.decode(partes[1]), StandardCharsets.UTF_8);
            String[] campos = payload.split("\\|", -1);
            if (campos.length != 4 || !"v1".equals(campos[0]) || campos[3].isBlank()) {
                throw new IllegalArgumentException();
            }

            Long idInscricao = Long.valueOf(campos[1]);
            Instant expiraEm = Instant.ofEpochSecond(Long.parseLong(campos[2]));
            if (idInscricao <= 0 || !expiraEm.isAfter(Instant.now())) {
                throw new RegraNegocioException("O QR Code é inválido ou expirou. Gere um novo código.");
            }
            return new DadosToken(idInscricao, expiraEm);
        } catch (RegraNegocioException e) {
            throw e;
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new RegraNegocioException("O QR Code é inválido ou foi alterado.");
        }
    }

    private byte[] calcularHmac(String conteudo) {
        try {
            byte[] chave = obterChave();
            Mac mac = Mac.getInstance(ALGORITMO);
            mac.init(new SecretKeySpec(chave, ALGORITMO));
            return mac.doFinal(conteudo.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Não foi possível validar o token de check-in.", e);
        }
    }

    private byte[] obterChave() {
        if (segredo == null || segredo.isBlank()) {
            throw new IllegalStateException(
                    "Configure a variável de ambiente EVENTOS_QR_SECRET com uma chave aleatória de pelo menos 32 bytes."
            );
        }
        byte[] chave = segredo.getBytes(StandardCharsets.UTF_8);
        if (chave.length < 32) {
            throw new IllegalStateException(
                    "EVENTOS_QR_SECRET precisa ter pelo menos 32 bytes."
            );
        }
        return chave;
    }
}
