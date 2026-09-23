package com.example.vendasestoque.dtos;

import jakarta.validation.constraints.NotBlank;

public record MovimentacaoUpdateDTO(

        @NotBlank
        String motivo
) {
}
