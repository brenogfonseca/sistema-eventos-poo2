package br.ueg.eventos.infrastructure.adapter.out.security;

import br.ueg.eventos.application.port.out.AuthenticationTokenPort;
import br.ueg.eventos.domain.model.Perfil;
import br.ueg.eventos.infrastructure.config.ApplicationEnvironment;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;

/** HMAC-signed bearer credentials for the web and desktop clients. */
public final class HmacAuthenticationTokenSigner implements AuthenticationTokenPort {
    private static final long DURACAO_SEGUNDOS = 8 * 60 * 60;
    private static final String ALGORITMO = "HmacSHA256";
    private final String segredo;

    public HmacAuthenticationTokenSigner() {
        this(ApplicationEnvironment.get("EVENTOS_AUTH_SECRET"));
    }

    HmacAuthenticationTokenSigner(String segredo) {
        this.segredo = segredo;
    }

    @Override
    public Token emitir(Integer idUsuario, String perfil) {
        if (idUsuario == null || idUsuario <= 0 || perfil == null) {
            throw new IllegalArgumentException("Usuário e perfil são obrigatórios para criar a sessão.");
        }
        Instant expiraEm = Instant.now().plusSeconds(DURACAO_SEGUNDOS);
        String payload = "v1|" + idUsuario + "|" + perfil + "|" + expiraEm.getEpochSecond();
        String codificado = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        String assinatura = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(hmac(codificado));
        return new Token("v1." + codificado + "." + assinatura, expiraEm);
    }

    @Override
    public Principal validar(String token) {
        try {
            if (token == null || token.length() > 4096) throw new IllegalArgumentException();
            String[] partes = token.split("\\.", -1);
            if (partes.length != 3 || !"v1".equals(partes[0])) throw new IllegalArgumentException();
            byte[] recebida = Base64.getUrlDecoder().decode(partes[2]);
            if (!MessageDigest.isEqual(hmac(partes[1]), recebida)) throw new IllegalArgumentException();
            String payload = new String(Base64.getUrlDecoder().decode(partes[1]), StandardCharsets.UTF_8);
            String[] campos = payload.split("\\|", -1);
            if (campos.length != 4 || !"v1".equals(campos[0])) throw new IllegalArgumentException();
            Integer id = Integer.valueOf(campos[1]);
            Perfil perfil = Perfil.valueOf(campos[2]);
            Instant expiraEm = Instant.ofEpochSecond(Long.parseLong(campos[3]));
            if (id <= 0 || !expiraEm.isAfter(Instant.now())) throw new IllegalArgumentException();
            return new Principal(id, perfil.name(), expiraEm);
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Token inválido ou expirado.");
        }
    }

    private byte[] hmac(String valor) {
        try {
            byte[] chave = obterChave();
            Mac mac = Mac.getInstance(ALGORITMO);
            mac.init(new SecretKeySpec(chave, ALGORITMO));
            return mac.doFinal(valor.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Não foi possível assinar a sessão.", e);
        }
    }

    private byte[] obterChave() {
        if (segredo == null || segredo.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("Configure EVENTOS_AUTH_SECRET com pelo menos 32 bytes.");
        }
        return segredo.getBytes(StandardCharsets.UTF_8);
    }
}
