package com.example.vendasestoque.repositories;

import com.example.vendasestoque.entities.ItemVenda;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemVendaRepository extends JpaRepository<ItemVenda, Long> {
}
