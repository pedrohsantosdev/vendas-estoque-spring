package com.example.vendasestoque.repositories;

import com.example.vendasestoque.VendasestoqueApplication;
import com.example.vendasestoque.entities.Produto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        classes = VendasestoqueApplication.class,
        properties = "spring.jpa.hibernate.ddl-auto=create"
)
@ActiveProfiles("test")
@Testcontainers
public class ProdutoRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static MySQLContainer mysql = new MySQLContainer("mysql:8.0.36")
            .withDatabaseName("produtos_test")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private ProdutoRepository produtoRepository;

    @BeforeEach
    void limparBanco() {
        produtoRepository.deleteAll();
    }

    private Produto cadastrarProduto(
            String nome,
            String codigo,
            Integer quantidadeEstoque,
            Integer estoqueMinimo
    ) {
        Produto produto = new Produto(
                null,
                nome,
                codigo,
                new BigDecimal("49.90"),
                quantidadeEstoque,
                estoqueMinimo
        );

        return produtoRepository.save(produto);
    }

    @Test
    void deveRetornarSomenteProdutosComEstoqueMenorOuIgualAoMinimo() {

        // Preparar
        Produto mouse = cadastrarProduto(
                "Mouse", "PRD-001", 3, 5
        );

        Produto teclado = cadastrarProduto(
                "Teclado", "PRD-002", 5, 5
        );

        Produto monitor = cadastrarProduto(
                "Monitor", "PRD-003", 8, 5
        );

        // Executar
        List<Produto> resultado =
                produtoRepository.findProdutosAbaixoDoEstoqueMinimo();

        // Verificar
        List<Long> idsRetornados = resultado.stream()
                .map(Produto::getId)
                .toList();

        assertEquals(2, resultado.size());
        assertTrue(idsRetornados.contains(mouse.getId()));
        assertTrue(idsRetornados.contains(teclado.getId()));
        assertFalse(idsRetornados.contains(monitor.getId()));
    }

    @Test
    void deveRetornarListaVaziaQuandoTodosProdutosEstiveremAcimaDoMinimo() {

        // Preparar
        cadastrarProduto("Mouse", "PRD-001", 6, 5);
        cadastrarProduto("Teclado", "PRD-002", 10, 5);

        // Executar
        List<Produto> resultado =
                produtoRepository.findProdutosAbaixoDoEstoqueMinimo();

        // Verificar
        assertTrue(resultado.isEmpty());
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoExistiremProdutos() {

        // Preparar: o banco já foi limpo pelo @BeforeEach.

        // Executar
        List<Produto> resultado =
                produtoRepository.findProdutosAbaixoDoEstoqueMinimo();

        // Verificar
        assertTrue(resultado.isEmpty());
    }
}
