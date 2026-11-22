package br.ueg.eventos.infrastructure.adapter.out.persistence;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Pbkdf2PasswordEncryptorTest {
    private final Pbkdf2PasswordEncryptor encryptor = new Pbkdf2PasswordEncryptor();

    @Test
    void gravaHashPBKDF2EConfereSenhaSemArmazenarTextoLimpo() {
        String senha = "SenhaForte@123";
        String hash = encryptor.criptografar(senha);

        assertTrue(hash.startsWith("pbkdf2-sha256$"));
        assertFalse(hash.contains(senha));
        assertTrue(encryptor.verificar(senha, hash));
        assertFalse(encryptor.verificar("SenhaErrada@123", hash));
        assertFalse(encryptor.precisaAtualizar(hash));
    }

    @Test
    void aceitaHashSha256AntigoSomenteParaMigrarNoLogin() throws Exception {
        byte[] bytes = MessageDigest.getInstance("SHA-256")
                .digest("SenhaForte@123".getBytes(StandardCharsets.UTF_8));
        String legado = HexFormat.of().formatHex(bytes);

        assertTrue(encryptor.verificar("SenhaForte@123", legado));
        assertTrue(encryptor.precisaAtualizar(legado));
    }
}
