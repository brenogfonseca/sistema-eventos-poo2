package br.ueg.eventos.application.port.out;

/**
 * Porta de saída: define o contrato para o serviço de criptografia de senhas.
 * A implementação concreta fica na camada de infraestrutura, mantendo o core
 * independente de qualquer biblioteca externa de hashing.
 */
public interface PasswordEncryptor {

    /**
     * Criptografa uma senha em texto plano e retorna seu hash.
     *
     * @param senhaLimpa a senha em texto plano a ser criptografada
     * @return o hash gerado da senha
     */
    String criptografar(String senhaLimpa);

    /** Verifica a senha informada contra o hash persistido. */
    default boolean verificar(String senhaLimpa, String hashPersistido) {
        if (senhaLimpa == null || hashPersistido == null) return false;
        return java.security.MessageDigest.isEqual(
                criptografar(senhaLimpa).getBytes(java.nio.charset.StandardCharsets.UTF_8),
                hashPersistido.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    /** Indica se um hash antigo deve ser atualizado após autenticação válida. */
    default boolean precisaAtualizar(String hashPersistido) {
        return false;
    }
}
