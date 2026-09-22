package com.example.vendasestoque.dtos;

import com.example.vendasestoque.entities.Produto;

import java.math.BigDecimal;

public record ProdutoResponseDTO(
        Long id,
        String nome,
        String codigo,
        BigDecimal preco,
        Integer quantidadeEstoque,
        Integer estoqueMinimo
) {

    public ProdutoResponseDTO(Produto produto) {
        this(
                produto.getId(),
                produto.getNome(),
                produto.getCodigo(),
                produto.getPreco(),
                produto.getQuantidadeEstoque(),
                produto.getEstoqueMinimo()
        );
    }
}
