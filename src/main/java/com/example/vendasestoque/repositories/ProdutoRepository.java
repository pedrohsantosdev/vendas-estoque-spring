package com.example.vendasestoque.repositories;

import com.example.vendasestoque.entities.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    Produto findByCodigo(String codigo);
}
