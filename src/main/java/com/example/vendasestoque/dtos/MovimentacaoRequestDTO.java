package com.example.vendasestoque.dtos;

import com.example.vendasestoque.entities.enuns.TipoMovimentacao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record MovimentacaoRequestDTO(

        @NotNull(message = "Campo não pode ser nulo")
        @Positive(message = "Campo não pode ser negativo")
        Long produtoId,

        @NotNull(message = "O tipo da movimentação é obrigatório")
        TipoMovimentacao tipoMovimentacao,

        @NotNull(message = "Campo não pode ser nulo")
        @Positive(message = "Campo não pode ser negativo")
        Integer quantidade,

        @NotBlank(message = "Campo não pode ser vazio")
        String motivo
) {
}