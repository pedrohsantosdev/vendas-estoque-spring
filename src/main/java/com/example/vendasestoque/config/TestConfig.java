package com.example.vendasestoque.config;

import com.example.vendasestoque.entities.Cliente;
import com.example.vendasestoque.entities.Produto;
import com.example.vendasestoque.repositories.ClienteRepository;
import com.example.vendasestoque.repositories.ProdutoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.Arrays;

@Configuration
public class TestConfig implements CommandLineRunner {

    private final ProdutoRepository produtoRepository;
    private final ClienteRepository clienteRepository;

    public TestConfig(ProdutoRepository produtoRepository, ClienteRepository clienteRepository) {
        this.produtoRepository = produtoRepository;
        this.clienteRepository = clienteRepository;
    }

    @Override
    public void run(String... args) {
        if (produtoRepository.count() == 0) {

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

        if(clienteRepository.count() == 0) {

            Cliente c1 = new Cliente(
                    null,
                    "Maria Silva",
                    "maria@example.com",
                    "11987654321"
            );

            Cliente c2 = new Cliente(
                    null,
                    "João Souza",
                    "joao@example.com",
                    "21987654321"
            );

            clienteRepository.saveAll(Arrays.asList(c1, c2));
        }
    }
}
