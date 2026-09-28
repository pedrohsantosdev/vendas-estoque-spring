package com.example.vendasestoque.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record VendaRequestDTO(

        @NotNull(message = "Campo obrigatório")
        @Positive(message = "Campo negativo não é válido")
        Long clienteId,

        @NotEmpty(message = "Lista não pode ser vazia")
        @Valid
        List<@NotNull(message = "Itens da lista não podem ser nulos") ItemVendaRequestDTO> itens
) {
}
