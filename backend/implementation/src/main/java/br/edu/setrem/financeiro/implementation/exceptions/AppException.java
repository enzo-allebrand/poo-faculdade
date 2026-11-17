package br.edu.setrem.financeiro.implementation.exceptions;

/**
 * Exceção base da camada de aplicação com código HTTP associado.
 */
public abstract class AppException extends RuntimeException {

    private final int statusCode;

    protected AppException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
