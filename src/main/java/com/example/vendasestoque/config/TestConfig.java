package com.example.vendasestoque.config;

import com.example.vendasestoque.entities.Cliente;
import com.example.vendasestoque.entities.ItemVenda;
import com.example.vendasestoque.entities.PK.ItemVendaPK;
import com.example.vendasestoque.entities.Produto;
import com.example.vendasestoque.entities.Venda;
import com.example.vendasestoque.entities.enuns.StatusVenda;
import com.example.vendasestoque.repositories.ClienteRepository;
import com.example.vendasestoque.repositories.ItemVendaRepository;
import com.example.vendasestoque.repositories.ProdutoRepository;
import com.example.vendasestoque.repositories.VendaRepository;
import jakarta.transaction.Transactional;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Configuration
public class TestConfig implements CommandLineRunner {

    private final ClienteRepository clienteRepository;
    private final ProdutoRepository produtoRepository;
    private final VendaRepository vendaRepository;
    private final ItemVendaRepository itemVendaRepository;

    public TestConfig(
            ClienteRepository clienteRepository,
            ProdutoRepository produtoRepository,
            VendaRepository vendaRepository,
            ItemVendaRepository itemVendaRepository) {

        this.clienteRepository = clienteRepository;
        this.produtoRepository = produtoRepository;
        this.vendaRepository = vendaRepository;
        this.itemVendaRepository = itemVendaRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {

        // Executa a população somente se as quatro tabelas estiverem vazias.
        if (clienteRepository.count() > 0
                || produtoRepository.count() > 0
                || vendaRepository.count() > 0
                || itemVendaRepository.count() > 0) {
            return;
        }

        // 1. Clientes
        Cliente c1 = new Cliente(
                null, "Maria Silva",
                "maria@example.com", "11987654321"
        );

        Cliente c2 = new Cliente(
                null, "João Souza",
                "joao@example.com", "21987654321"
        );

        clienteRepository.saveAll(List.of(c1, c2));

        // 2. Produtos
        Produto p1 = new Produto(
                null, "Mouse", "PRD-001",
                new BigDecimal("49.90"), 10, 2
        );

        Produto p2 = new Produto(
                null, "Teclado", "PRD-002",
                new BigDecimal("89.90"), 5, 1
        );

        produtoRepository.saveAll(List.of(p1, p2));

        // 3. Vendas vinculadas aos clientes salvos
        Venda v1 = new Venda(
                null,
                Instant.parse("2026-09-21T13:00:00Z"),
                StatusVenda.CONFIRMADA,
                c1
        );

        Venda v2 = new Venda(
                null,
                Instant.parse("2026-09-21T14:00:00Z"),
                StatusVenda.CANCELADA,
                c2
        );

        vendaRepository.saveAll(List.of(v1, v2));

        // 4. Itens vinculados às vendas e aos produtos salvos
        ItemVenda item1 = new ItemVenda(
                new ItemVendaPK(v1, p1),
                2,
                p1.getPreco()
        );

        ItemVenda item2 = new ItemVenda(
                new ItemVendaPK(v1, p2),
                1,
                p2.getPreco()
        );

        ItemVenda item3 = new ItemVenda(
                new ItemVendaPK(v2, p1),
                1,
                p1.getPreco()
        );

        itemVendaRepository.saveAll(List.of(item1, item2, item3));
    }
}
