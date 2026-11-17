package br.edu.setrem.financeiro.implementation.exceptions;

/**
 * Erro de validação ou violação de regra de negócio (HTTP 400).
 */
public class RegraNegocioException extends AppException {

    public RegraNegocioException(String message) {
        super(400, message);
    }
}
