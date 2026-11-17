package br.edu.setrem.financeiro.implementation.exceptions;

/**
 * Erro de autenticação ou credenciais inválidas (HTTP 401).
 */
public class NaoAutorizadoException extends AppException {

    public NaoAutorizadoException(String message) {
        super(401, message);
    }
}
