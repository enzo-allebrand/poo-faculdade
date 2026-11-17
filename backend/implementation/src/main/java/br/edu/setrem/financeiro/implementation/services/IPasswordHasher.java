package br.edu.setrem.financeiro.implementation.services;

/**
 * Abstração para geração e verificação de hash de senhas.
 */
public interface IPasswordHasher {
    String hash(String senhaPura);
    boolean matches(String senhaPura, String senhaHash);
}
