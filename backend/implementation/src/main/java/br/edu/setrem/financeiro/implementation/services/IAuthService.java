package br.edu.setrem.financeiro.implementation.services;

import br.edu.setrem.financeiro.implementation.models.AuthModels;

/**
 * Contrato de autenticação e cadastro de usuários.
 */
public interface IAuthService {
    AuthModels.AuthResult registrar(AuthModels.RegisterRequest request);
    AuthModels.AuthResult autenticar(AuthModels.LoginRequest request);
    AuthModels.UserResponse obterUsuarioAtual(Long usuarioId);
}
