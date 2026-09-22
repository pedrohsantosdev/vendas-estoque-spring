package com.example.vendasestoque.dtos;

import com.example.vendasestoque.entities.ItemVenda;

import java.math.BigDecimal;

public record ItemVendaResponseDTO(
        Long produtoId,
        String produtoNome,
        String codigoProduto,
        Integer quantidade,
        BigDecimal precoUnitario,
        BigDecimal subTotal
) {
    public ItemVendaResponseDTO(ItemVenda itemVenda) {
        this(
                itemVenda.getId().getProduto().getId(),
                itemVenda.getId().getProduto().getNome(),
                itemVenda.getId().getProduto().getCodigo(),
                itemVenda.getQuantidade(),
                itemVenda.getPrecoUnitario(),
                itemVenda.getSubTotal()
        );
    }
}
