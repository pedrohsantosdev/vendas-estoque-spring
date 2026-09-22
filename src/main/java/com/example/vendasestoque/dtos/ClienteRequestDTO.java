package com.example.vendasestoque.dtos;

import jakarta.validation.constraints.NotBlank;

public record ClienteRequestDTO(

        @NotBlank(message = "Campo não pode ser nulo")
        String nome,
        @NotBlank(message = "Campo não pode ser nulo")
        String email,
        @NotBlank(message = "Campo não pode ser nulo")
        String telefone
) {
}
