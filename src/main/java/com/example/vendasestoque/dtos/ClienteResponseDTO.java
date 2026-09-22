package com.example.vendasestoque.dtos;

import com.example.vendasestoque.entities.Cliente;

public record ClienteResponseDTO(
        Long id,
        String nome,
        String email,
        String telefone
) {

    public ClienteResponseDTO(Cliente cliente) {
        this(cliente.getId(), cliente.getNome(), cliente.getEmail(), cliente.getTelefone());
    }
}
