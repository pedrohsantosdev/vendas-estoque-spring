package com.example.vendasestoque.config;

import com.example.vendasestoque.entities.Produto;
import com.example.vendasestoque.repositories.ProdutoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.Arrays;

@Configuration
public class TestConfig implements CommandLineRunner {

    private final ProdutoRepository produtoRepository;

    public TestConfig(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    @Override
    public void run(String... args) {
        if (produtoRepository.count() > 0) {
            return;
        }

        Produto p1 = new Produto(
                null, "Mouse", "PRD-001",
                new BigDecimal("49.90"), 10, 2
        );

        Produto p2 = new Produto(
                null, "Teclado", "PRD-002",
                new BigDecimal("89.90"), 1, 2
        );

        produtoRepository.saveAll(Arrays.asList(p1, p2));
    }
}
