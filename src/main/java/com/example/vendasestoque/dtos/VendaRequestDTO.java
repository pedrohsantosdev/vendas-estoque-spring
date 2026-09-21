package com.example.vendasestoque.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record VendaRequestDTO(

        @NotNull(message = "Campo obrigatório")
        @Positive(message = "Campo negativo não é válido")
        Long clienteId,

        @NotNull(message = "Campo obrigatório")
        @Positive(message = "Campo negativo não é válido")
        Long produtoId,

        @NotNull(message = "Campo obrigatório")
        @Positive(message = "Campo negativo não é válido")
        Integer quantidade
) {
}
