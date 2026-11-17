package br.edu.setrem.financeiro.implementation.exceptions;

/**
 * Erro de conflito de integridade ou duplicidade (HTTP 409).
 */
public class ConflitoException extends AppException {

    public ConflitoException(String message) {
        super(409, message);
    }
}
