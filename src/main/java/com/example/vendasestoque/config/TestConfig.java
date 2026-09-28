package com.example.vendasestoque.config;

import com.example.vendasestoque.entities.*;
import com.example.vendasestoque.entities.PK.ItemVendaPK;
import com.example.vendasestoque.entities.enuns.StatusVenda;
import com.example.vendasestoque.entities.enuns.TipoMovimentacao;
import com.example.vendasestoque.repositories.*;
import com.example.vendasestoque.services.MovimentacaoEstoqueService;
import jakarta.transaction.Transactional;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Configuration
@Profile("!test")
public class TestConfig implements CommandLineRunner {

    private final ClienteRepository clienteRepository;
    private final ProdutoRepository produtoRepository;
    private final VendaRepository vendaRepository;
    private final ItemVendaRepository itemVendaRepository;
    private final MovimentacaoEstoqueService movimentacaoEstoqueService;


    public TestConfig(
            ClienteRepository clienteRepository,
            ProdutoRepository produtoRepository,
            VendaRepository vendaRepository,
            ItemVendaRepository itemVendaRepository,
            MovimentacaoEstoqueService movimentacaoEstoqueService) {

        this.clienteRepository = clienteRepository;
        this.produtoRepository = produtoRepository;
        this.vendaRepository = vendaRepository;
        this.itemVendaRepository = itemVendaRepository;
        this.movimentacaoEstoqueService = movimentacaoEstoqueService;
    }

    @Override
    @Transactional
    public void run(String... args) {

        // Cria os dados iniciais somente quando as tabelas estão vazias.
        if (clienteRepository.count() == 0
                && produtoRepository.count() == 0
                && vendaRepository.count() == 0
                && itemVendaRepository.count() == 0) {

            Cliente c1 = new Cliente(
                    null, "Maria Silva",
                    "maria@example.com", "11987654321"
            );

            Cliente c2 = new Cliente(
                    null, "João Souza",
                    "joao@example.com", "21987654321"
            );

            clienteRepository.saveAll(List.of(c1, c2));

            Produto p1 = new Produto(
                    null, "Mouse", "PRD-001",
                    new BigDecimal("49.90"), 10, 2
            );

            Produto p2 = new Produto(
                    null, "Teclado", "PRD-002",
                    new BigDecimal("89.90"), 5, 1
            );

            produtoRepository.saveAll(List.of(p1, p2));

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

            ItemVenda item1 = new ItemVenda(
                    new ItemVendaPK(v1, p1), 2, p1.getPreco()
            );

            ItemVenda item2 = new ItemVenda(
                    new ItemVendaPK(v1, p2), 1, p2.getPreco()
            );

            ItemVenda item3 = new ItemVenda(
                    new ItemVendaPK(v2, p1), 1, p1.getPreco()
            );

            itemVendaRepository.saveAll(List.of(item1, item2, item3));
        }

        Produto produto = produtoRepository.findByCodigo("PRD-001");


        if (produto == null) {
            throw new IllegalStateException(
                    "Cadastre o produto PRD-001 para executar este teste"
            );
        }


        MovimentacaoEstoque entrada = new MovimentacaoEstoque(
                null,
                TipoMovimentacao.ENTRADA,
                5,
                Instant.now(),
                "Reposição de estoque",
                produto,
                null
        );

        //movimentacaoEstoqueService.cadastrarMovimentacao(entrada);
    }
}
