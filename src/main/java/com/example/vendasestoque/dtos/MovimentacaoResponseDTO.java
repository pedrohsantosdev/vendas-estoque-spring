package com.example.vendasestoque.dtos;

import com.example.vendasestoque.entities.MovimentacaoEstoque;
import com.example.vendasestoque.entities.enuns.TipoMovimentacao;

import java.time.Instant;

public record MovimentacaoResponseDTO(

        Long id,
        TipoMovimentacao tipoMovimentacao,
        Integer quantidade,
        Instant momento,
        String motivo,
        Long produtoId,
        Long vendaId
) {

    public MovimentacaoResponseDTO(MovimentacaoEstoque movimentacaoEstoque) {
        this(
                movimentacaoEstoque.getId(),
                movimentacaoEstoque.getTipoMovimentacao(),
                movimentacaoEstoque.getQuantidade(),
                movimentacaoEstoque.getMomento(),
                movimentacaoEstoque.getMotivo(),
                movimentacaoEstoque.getProduto().getId(),
                obterVenda(movimentacaoEstoque)
        );
    }

    private static Long obterVenda(MovimentacaoEstoque movimentacaoEstoque) {

        if(movimentacaoEstoque.getVenda() == null) {
            return null;
        }

        return movimentacaoEstoque.getVenda().getId();

    }
}
