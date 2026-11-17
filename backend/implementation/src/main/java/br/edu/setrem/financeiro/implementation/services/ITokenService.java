package br.edu.setrem.financeiro.implementation.services;

import br.edu.setrem.financeiro.domain.entities.Usuario;

/**
 * Abstração para emissão de tokens JWT.
 */
public interface ITokenService {
    String gerarToken(Usuario usuario);
}
