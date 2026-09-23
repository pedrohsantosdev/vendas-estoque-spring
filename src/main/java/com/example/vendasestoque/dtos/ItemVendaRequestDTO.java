package com.example.vendasestoque.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ItemVendaRequestDTO(

        @NotNull(message = "campo não pode ser nulo")
        @Positive(message = "campo não pode ser negativo")
        Long produtoId,

        @NotNull(message = "campo não pode ser nulo")
        @Positive(message = "campo não pode ser negativo")
        Integer quantidade
) {
}
