package br.edu.setrem.financeiro.implementation.exceptions;

/**
 * Erro quando um recurso solicitado não existe ou não pertence ao usuário (HTTP 404).
 */
public class RecursoNaoEncontradoException extends AppException {

    public RecursoNaoEncontradoException(String message) {
        super(404, message);
    }
}
