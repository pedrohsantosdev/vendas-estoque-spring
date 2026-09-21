package com.example.vendasestoque.repositories;

import com.example.vendasestoque.entities.ItemVenda;
import com.example.vendasestoque.entities.PK.ItemVendaPK;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemVendaRepository extends JpaRepository<ItemVenda, ItemVendaPK> {
}
