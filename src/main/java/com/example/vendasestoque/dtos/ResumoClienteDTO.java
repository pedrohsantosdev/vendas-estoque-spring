package com.example.vendasestoque.dtos;

import com.example.vendasestoque.entities.Cliente;

public record ResumoClienteDTO(
        Long clienteId,
        String clienteNome
) {

    public ResumoClienteDTO(Cliente cliente) {
        this( cliente.getId(), cliente.getNome());
    }
}
