package com.example.vendasestoque.services;

import com.example.vendasestoque.VendasestoqueApplication;
import com.example.vendasestoque.entities.Cliente;
import com.example.vendasestoque.entities.ItemVenda;
import com.example.vendasestoque.entities.MovimentacaoEstoque;
import com.example.vendasestoque.entities.PK.ItemVendaPK;
import com.example.vendasestoque.entities.Produto;
import com.example.vendasestoque.entities.Venda;
import com.example.vendasestoque.entities.enuns.StatusVenda;
import com.example.vendasestoque.entities.enuns.TipoMovimentacao;
import com.example.vendasestoque.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        classes = VendasestoqueApplication.class,
        properties = "spring.jpa.hibernate.ddl-auto=create"
)
@ActiveProfiles("test")
@Testcontainers
public class ExclusaoIntegrationTest {

    @Container
    @ServiceConnection
    static MySQLContainer mysql = new MySQLContainer("mysql:8.0.36")
            .withDatabaseName("exclusao_test")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private VendaRepository vendaRepository;

    @Autowired
    private ItemVendaRepository itemVendaRepository;

    @Autowired
    private MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private VendaService vendaService;

    @BeforeEach
    void limparBanco() {
        movimentacaoEstoqueRepository.deleteAll();
        itemVendaRepository.deleteAll();
        vendaRepository.deleteAll();
        produtoRepository.deleteAll();
        clienteRepository.deleteAll();
    }

    private Cliente cadastrarCliente() {
        Cliente cliente = new Cliente(
                null,
                "Maria Silva",
                "maria@example.com",
                "11987654321"
        );

        return clienteRepository.save(cliente);
    }

    private Produto cadastrarProduto() {
        Produto produto = new Produto(
                null,
                "Mouse",
                "PRD-001",
                new BigDecimal("49.90"),
                5,
                2
        );

        return produtoRepository.save(produto);
    }

    private Venda cadastrarVenda(Cliente cliente) {
        Venda venda = new Venda(
                null,
                Instant.now(),
                StatusVenda.CONFIRMADA,
                cliente
        );

        return vendaRepository.save(venda);
    }

    @Test
    void deveImpedirExclusaoDeClienteComVendaVinculada() {

        // Preparar
        Cliente cliente = cadastrarCliente();
        Venda venda = cadastrarVenda(cliente);

        Long clienteId = cliente.getId();

        // Executar e verificar a rejeição
        assertThrows(
                DataIntegrityViolationException.class,
                () -> clienteService.deletarCliente(clienteId)
        );

        // Verificar que os registros continuam no banco
        assertTrue(clienteRepository.existsById(clienteId));
        assertTrue(vendaRepository.existsById(venda.getId()));

        Venda vendaBanco = vendaRepository.findById(venda.getId())
                .orElseThrow();

        assertEquals(clienteId, vendaBanco.getCliente().getId());
    }

    @Test
    void deveImpedirExclusaoDeProdutoComMovimentacaoVinculada() {

        // Preparar
        Produto produto = cadastrarProduto();

        MovimentacaoEstoque movimentacao = new MovimentacaoEstoque(
                null,
                TipoMovimentacao.ENTRADA,
                5,
                Instant.now(),
                "Entrada para teste",
                produto,
                null
        );

        movimentacao = movimentacaoEstoqueRepository.save(movimentacao);

        Long produtoId = produto.getId();

        // Executar e verificar a rejeição
        assertThrows(
                DataIntegrityViolationException.class,
                () -> produtoService.deletarProduto(produtoId)
        );

        // Verificar que os registros continuam no banco
        assertTrue(produtoRepository.existsById(produtoId));
        assertTrue(
                movimentacaoEstoqueRepository.existsById(movimentacao.getId())
        );

        Produto produtoBanco = produtoRepository.findById(produtoId)
                .orElseThrow();

        assertEquals(5, produtoBanco.getQuantidadeEstoque());

        MovimentacaoEstoque movimentacaoBanco =
                movimentacaoEstoqueRepository.findById(movimentacao.getId())
                        .orElseThrow();

        assertEquals(produtoId, movimentacaoBanco.getProduto().getId());
    }

    @Test
    void deveImpedirExclusaoDeVendaComItemVinculado() {

        // Preparar
        Cliente cliente = cadastrarCliente();
        Produto produto = cadastrarProduto();
        Venda venda = cadastrarVenda(cliente);

        ItemVenda item = new ItemVenda(
                new ItemVendaPK(venda, produto),
                2,
                produto.getPreco()
        );

        item = itemVendaRepository.save(item);

        Long vendaId = venda.getId();

        // Executar e verificar a rejeição
        assertThrows(
                DataIntegrityViolationException.class,
                () -> vendaService.deletarVenda(vendaId)
        );

        // Verificar que os registros continuam no banco
        assertTrue(vendaRepository.existsById(vendaId));
        assertTrue(itemVendaRepository.existsById(item.getId()));
        assertTrue(produtoRepository.existsById(produto.getId()));
        assertTrue(clienteRepository.existsById(cliente.getId()));

        Venda vendaBanco = vendaRepository.findById(vendaId)
                .orElseThrow();

        assertEquals(StatusVenda.CONFIRMADA, vendaBanco.getStatusVenda());
    }
}