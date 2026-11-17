package br.edu.setrem.financeiro.application.services;

import br.edu.setrem.financeiro.domain.entities.Categoria;
import br.edu.setrem.financeiro.domain.enums.TipoCategoriaEnum;
import br.edu.setrem.financeiro.implementation.exceptions.ConflitoException;
import br.edu.setrem.financeiro.implementation.exceptions.RecursoNaoEncontradoException;
import br.edu.setrem.financeiro.implementation.exceptions.RegraNegocioException;
import br.edu.setrem.financeiro.implementation.models.CategoriaModels;
import br.edu.setrem.financeiro.implementation.repositories.ICategoriaRepository;
import br.edu.setrem.financeiro.implementation.repositories.ITransacaoRepository;
import br.edu.setrem.financeiro.implementation.services.ICategoriaService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

/**
 * Regras de negócio para gerenciamento de categorias.
 */
@ApplicationScoped
public class CategoriaService implements ICategoriaService {

    private final ICategoriaRepository categoriaRepository;
    private final ITransacaoRepository transacaoRepository;

    @Inject
    public CategoriaService(ICategoriaRepository categoriaRepository, ITransacaoRepository transacaoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.transacaoRepository = transacaoRepository;
    }

    @Override
    public List<CategoriaModels.CategoriaDTO> listar(Long usuarioId) {
        return categoriaRepository.listarPorUsuario(usuarioId)
                .stream()
                .map(CategoriaModels.CategoriaDTO::from)
                .toList();
    }

    @Override
    @Transactional
    public CategoriaModels.CategoriaDTO criar(Long usuarioId, CategoriaModels.CategoriaRequest request) {
        TipoCategoriaEnum tipo = resolverTipo(request.tipo());
        Categoria categoria = new Categoria();
        categoria.setDescricao(request.descricao().trim());
        categoria.setTipo(tipo);
        categoria.setUsuarioId(usuarioId);

        return CategoriaModels.CategoriaDTO.from(categoriaRepository.salvar(categoria));
    }

    @Override
    @Transactional
    public CategoriaModels.CategoriaDTO atualizar(Long usuarioId, Long categoriaId, CategoriaModels.CategoriaRequest request) {
        Categoria categoria = categoriaRepository.buscarPorIdEUsuario(categoriaId, usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria não encontrada."));

        categoria.setDescricao(request.descricao().trim());
        if (request.tipo() != null && !request.tipo().isBlank()) {
            categoria.setTipo(resolverTipo(request.tipo()));
        }

        return CategoriaModels.CategoriaDTO.from(categoriaRepository.salvar(categoria));
    }

    @Override
    @Transactional
    public void excluir(Long usuarioId, Long categoriaId) {
        categoriaRepository.buscarPorIdEUsuario(categoriaId, usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria não encontrada."));

        if (transacaoRepository.contarPorCategoria(categoriaId, usuarioId) > 0) {
            throw new ConflitoException("Não é possível excluir uma categoria que possui transações vinculadas.");
        }

        categoriaRepository.excluir(categoriaId, usuarioId);
    }

    private TipoCategoriaEnum resolverTipo(String codigo) {
        TipoCategoriaEnum tipo = TipoCategoriaEnum.fromCodigo(codigo);
        if (tipo == null) {
            throw new RegraNegocioException("Tipo de categoria inválido.");
        }
        return tipo;
    }
}
