package com.example.vendasestoque.dtos;

import com.example.vendasestoque.entities.MovimentacaoEstoque;

import java.time.Instant;

public record MovimentacaoResponseDTO(

        Long id,
        Integer tipoMovimentacao,
        Integer quantidade,
        Instant momento,
        String motivo,
        Long produtoId,
        Long vendaId
) {

    public MovimentacaoResponseDTO(MovimentacaoEstoque movimentacaoEstoque) {
        this(
                movimentacaoEstoque.getId(),
                movimentacaoEstoque.getTipoMovimentacao().getCode(),
                movimentacaoEstoque.getQuantidade(),
                movimentacaoEstoque.getMomento(),
                movimentacaoEstoque.getMotivo(),
                movimentacaoEstoque.getProduto().getId(),
                movimentacaoEstoque.getVenda().getId()
        );
    }
}
