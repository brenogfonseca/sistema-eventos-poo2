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
}
