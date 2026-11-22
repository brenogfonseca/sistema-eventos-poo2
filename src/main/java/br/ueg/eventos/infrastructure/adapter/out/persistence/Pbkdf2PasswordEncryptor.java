package br.ueg.eventos.infrastructure.adapter.out.persistence;

import br.ueg.eventos.application.port.out.PasswordEncryptor;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/** Password hashing with PBKDF2-HMAC-SHA256, an individual random salt and constant-time comparison. */
public final class Pbkdf2PasswordEncryptor implements PasswordEncryptor {
    private static final String PREFIXO = "pbkdf2-sha256";
    private static final String ALGORITMO = "PBKDF2WithHmacSHA256";
    private static final int ITERACOES = 210_000;
    private static final int TAMANHO_SALT = 16;
    private static final int TAMANHO_HASH_BITS = 256;
    private final SecureRandom random = new SecureRandom();

    @Override
    public String criptografar(String senhaLimpa) {
        if (senhaLimpa == null || senhaLimpa.isEmpty()) {
            throw new IllegalArgumentException("A senha é obrigatória.");
        }
        byte[] salt = new byte[TAMANHO_SALT];
        random.nextBytes(salt);
        byte[] hash = derivar(senhaLimpa, salt, ITERACOES);
        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        return PREFIXO + "$" + ITERACOES + "$" + encoder.encodeToString(salt)
                + "$" + encoder.encodeToString(hash);
    }

    @Override
    public boolean verificar(String senhaLimpa, String hashPersistido) {
        if (senhaLimpa == null || hashPersistido == null || hashPersistido.isBlank()) return false;
        String[] partes = hashPersistido.split("\\$", -1);
        if (partes.length == 4 && PREFIXO.equals(partes[0])) {
            try {
                int iteracoes = Integer.parseInt(partes[1]);
                if (iteracoes < 100_000 || iteracoes > 2_000_000) return false;
                Base64.Decoder decoder = Base64.getUrlDecoder();
                byte[] salt = decoder.decode(partes[2]);
                byte[] hashEsperado = decoder.decode(partes[3]);
                return MessageDigest.isEqual(hashEsperado, derivar(senhaLimpa, salt, iteracoes));
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
        // Verificação temporária de contas antigas; o login válido troca o hash pelo formato atual.
        if (hashPersistido.matches("(?i)[0-9a-f]{64}")) {
            try {
                byte[] antigo = MessageDigest.getInstance("SHA-256")
                        .digest(senhaLimpa.getBytes(StandardCharsets.UTF_8));
                return MessageDigest.isEqual(hexadecimal(antigo).getBytes(StandardCharsets.US_ASCII),
                        hashPersistido.toLowerCase().getBytes(StandardCharsets.US_ASCII));
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean precisaAtualizar(String hashPersistido) {
        return hashPersistido == null || !hashPersistido.startsWith(PREFIXO + "$");
    }

    private byte[] derivar(String senha, byte[] salt, int iteracoes) {
        PBEKeySpec spec = new PBEKeySpec(senha.toCharArray(), salt, iteracoes, TAMANHO_HASH_BITS);
        try {
            return SecretKeyFactory.getInstance(ALGORITMO).generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new IllegalStateException("Não foi possível derivar o hash da senha.", e);
        } finally {
            spec.clearPassword();
        }
    }

    private String hexadecimal(byte[] bytes) {
        StringBuilder resultado = new StringBuilder(bytes.length * 2);
        for (byte valor : bytes) resultado.append(String.format("%02x", valor));
        return resultado.toString();
    }
}
