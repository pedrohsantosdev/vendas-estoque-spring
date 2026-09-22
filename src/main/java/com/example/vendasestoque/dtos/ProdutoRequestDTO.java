package com.example.vendasestoque.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record ProdutoRequestDTO(
        @NotBlank(message = "Campo não pode ser vazio")
        String nome,

        @NotBlank(message = "Campo não pode ser vazio")
        String codigo,

        @NotNull(message = "Campo não pode ser nulo")
        @Positive(message = "Campo não pode ser negativo ou igual a zero")
        BigDecimal preco,

        @NotNull(message = "Campo não pode ser nulo")
        @PositiveOrZero(message = "Campo não pode ser negativo")
        Integer estoqueMinimo
) {
}
