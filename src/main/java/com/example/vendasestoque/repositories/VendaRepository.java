package com.example.vendasestoque.repositories;

import com.example.vendasestoque.entities.Venda;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VendaRepository extends JpaRepository<Venda, Long> {
}
