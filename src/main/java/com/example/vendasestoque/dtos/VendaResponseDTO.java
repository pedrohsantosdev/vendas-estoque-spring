package com.example.vendasestoque.dtos;

import com.example.vendasestoque.entities.Venda;
import com.example.vendasestoque.entities.enuns.StatusVenda;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record VendaResponseDTO(
        Long id,
        Instant momento,
        StatusVenda statusVenda,
        ResumoClienteDTO cliente,
        List<ItemVendaResponseDTO> itens,
        BigDecimal valorTotal
) {

    public VendaResponseDTO(Venda venda) {
        this(
                venda.getId(), venda.getMomento(), venda.getStatusVenda(), new ResumoClienteDTO(venda.getCliente()), venda.getItens().stream().map(
                        itemVenda -> new ItemVendaResponseDTO(itemVenda)
                ).toList(), venda.getTotal()
        );
    }
}
