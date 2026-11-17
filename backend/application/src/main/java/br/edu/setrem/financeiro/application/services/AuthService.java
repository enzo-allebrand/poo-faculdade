package br.edu.setrem.financeiro.application.services;

import br.edu.setrem.financeiro.domain.entities.Categoria;
import br.edu.setrem.financeiro.domain.entities.Usuario;
import br.edu.setrem.financeiro.domain.enums.TipoCategoriaEnum;
import br.edu.setrem.financeiro.implementation.exceptions.ConflitoException;
import br.edu.setrem.financeiro.implementation.exceptions.NaoAutorizadoException;
import br.edu.setrem.financeiro.implementation.models.AuthModels;
import br.edu.setrem.financeiro.implementation.repositories.ICategoriaRepository;
import br.edu.setrem.financeiro.implementation.repositories.IUsuarioRepository;
import br.edu.setrem.financeiro.implementation.services.IAuthService;
import br.edu.setrem.financeiro.implementation.services.IPasswordHasher;
import br.edu.setrem.financeiro.implementation.services.ITokenService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

/**
 * Regras de negócio de registro, login e perfil do usuário autenticado.
 */
@ApplicationScoped
public class AuthService implements IAuthService {

    private static final List<Object[]> CATEGORIAS_PADRAO = List.of(
            new Object[]{"Salário", TipoCategoriaEnum.R},
            new Object[]{"Freelance", TipoCategoriaEnum.R},
            new Object[]{"Alimentação", TipoCategoriaEnum.D},
            new Object[]{"Moradia", TipoCategoriaEnum.D},
            new Object[]{"Transporte", TipoCategoriaEnum.D},
            new Object[]{"Lazer", TipoCategoriaEnum.D},
            new Object[]{"Transferência", TipoCategoriaEnum.A}
    );

    private final IUsuarioRepository usuarioRepository;
    private final ICategoriaRepository categoriaRepository;
    private final IPasswordHasher passwordHasher;
    private final ITokenService tokenService;

    @Inject
    public AuthService(IUsuarioRepository usuarioRepository, ICategoriaRepository categoriaRepository, IPasswordHasher passwordHasher, ITokenService tokenService) {
        this.usuarioRepository = usuarioRepository;
        this.categoriaRepository = categoriaRepository;
        this.passwordHasher = passwordHasher;
        this.tokenService = tokenService;
    }

    @Override
    @Transactional
    public AuthModels.AuthResult registrar(AuthModels.RegisterRequest request) {
        String emailNormalizado = request.email().trim().toLowerCase();
        if (usuarioRepository.buscarPorEmail(emailNormalizado).isPresent()) {
            throw new ConflitoException("Já existe um usuário cadastrado com este e-mail.");
        }

        Usuario novoUsuario = new Usuario();
        novoUsuario.setNome(request.nome().trim());
        novoUsuario.setEmail(emailNormalizado);
        novoUsuario.setSenhaHash(passwordHasher.hash(request.senha()));
        Usuario salvo = usuarioRepository.salvar(novoUsuario);

        for (Object[] item : CATEGORIAS_PADRAO) {
            Categoria cat = new Categoria();
            cat.setDescricao((String) item[0]);
            cat.setTipo((TipoCategoriaEnum) item[1]);
            cat.setUsuarioId(salvo.getId());
            categoriaRepository.salvar(cat);
        }

        return new AuthModels.AuthResult(AuthModels.UserResponse.from(salvo), tokenService.gerarToken(salvo));
    }

    @Override
    public AuthModels.AuthResult autenticar(AuthModels.LoginRequest request) {
        String emailNormalizado = request.email().trim().toLowerCase();
        Usuario usuario = usuarioRepository.buscarPorEmail(emailNormalizado)
                .orElseThrow(() -> new NaoAutorizadoException("E-mail ou senha inválidos."));

        if (!passwordHasher.matches(request.senha(), usuario.getSenhaHash())) {
            throw new NaoAutorizadoException("E-mail ou senha inválidos.");
        }

        return new AuthModels.AuthResult(AuthModels.UserResponse.from(usuario), tokenService.gerarToken(usuario));
    }

    @Override
    public AuthModels.UserResponse obterUsuarioAtual(Long usuarioId) {
        Usuario usuario = usuarioRepository.buscarPorId(usuarioId)
                .orElseThrow(() -> new NaoAutorizadoException("Usuário não encontrado."));
        return AuthModels.UserResponse.from(usuario);
    }
}
