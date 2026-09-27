package br.ueg.eventos.infrastructure.adapter.out.persistence;

import br.ueg.eventos.application.port.out.PasswordEncryptor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Adaptador de saída: implementação de criptografia de senha usando SHA-256.
 * Fica na infraestrutura para isolar o core de qualquer detalhe técnico de hashing.
 * Não requer dependência externa — usa a API padrão do Java (java.security).
 *
 * <p><strong>Nota:</strong> Para produção, recomenda-se substituir por BCrypt ou
 * Argon2 (com salt), que são mais resistentes a ataques de força bruta.</p>
 */
public class Sha256PasswordEncryptor implements PasswordEncryptor {

    @Override
    public String criptografar(String senhaLimpa) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(senhaLimpa.getBytes(StandardCharsets.UTF_8));
            return bytesParaHexadecimal(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 é garantido pelo padrão Java, logo este bloco nunca deve ser atingido
            throw new IllegalStateException("Algoritmo SHA-256 não disponível na JVM.", e);
        }
    }

    private String bytesParaHexadecimal(byte[] bytes) {
        StringBuilder hexadecimal = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            hexadecimal.append(String.format("%02x", b));
        }
        return hexadecimal.toString();
    }
}
