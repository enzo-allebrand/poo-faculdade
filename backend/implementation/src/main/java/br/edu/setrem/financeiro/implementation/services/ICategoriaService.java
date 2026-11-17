package br.edu.setrem.financeiro.implementation.services;

import br.edu.setrem.financeiro.implementation.models.CategoriaModels;

import java.util.List;

/**
 * Contrato de gerenciamento de categorias financeiras.
 */
public interface ICategoriaService {
    List<CategoriaModels.CategoriaDTO> listar(Long usuarioId);
    CategoriaModels.CategoriaDTO criar(Long usuarioId, CategoriaModels.CategoriaRequest request);
    CategoriaModels.CategoriaDTO atualizar(Long usuarioId, Long categoriaId, CategoriaModels.CategoriaRequest request);
    void excluir(Long usuarioId, Long categoriaId);
}
