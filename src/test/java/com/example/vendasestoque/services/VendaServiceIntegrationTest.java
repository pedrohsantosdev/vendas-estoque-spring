package com.example.vendasestoque.services;

import com.example.vendasestoque.VendasestoqueApplication;
import com.example.vendasestoque.dtos.ItemVendaRequestDTO;
import com.example.vendasestoque.dtos.VendaRequestDTO;
import com.example.vendasestoque.entities.Cliente;
import com.example.vendasestoque.entities.Produto;
import com.example.vendasestoque.entities.Venda;
import com.example.vendasestoque.entities.enuns.StatusVenda;
import com.example.vendasestoque.repositories.*;
import com.example.vendasestoque.services.exceptions.EstoqueInsuficiente;
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
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest (
        classes = VendasestoqueApplication.class,
        properties = "spring.jpa.hibernate.ddl-auto=create"
)
@ActiveProfiles("test")
@Testcontainers
public class VendaServiceIntegrationTest {

    @Container
    @ServiceConnection
    static MySQLContainer mysql =
            new MySQLContainer("mysql:8.0.36").
                    withDatabaseName("vendaestoque_test").
                    withUsername("test").
                    withPassword("test");

    @Autowired
    private VendaRepository vendaRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private ItemVendaRepository itemVendaRepository;

    @Autowired
    private MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;

    @Autowired
    private VendaService vendaService;

    @Test
    void deveIniciarBancoSemVendas() {

        long quantidade = vendaRepository.count();

        assertEquals(0, quantidade);
    }

    @BeforeEach
    void limparBanco() {

        movimentacaoEstoqueRepository.deleteAll();
        itemVendaRepository.deleteAll();
        vendaRepository.deleteAll();
        produtoRepository.deleteAll();
        clienteRepository.deleteAll();

    }

    private Cliente cadastrarClienteParaTeste() {
        Cliente c1 = new Cliente(
                null, "Maria Silva",
                "maria@example.com", "11987654321"
        );
        c1 = clienteRepository.save(c1);
        return c1;
    }

    private Produto cadastrarProdutoParaTeste(String nome, String codigo, BigDecimal preco, Integer quantidadeEstoque, Integer estoqueMinimo) {
        Produto p1 = new Produto(
                null, nome, codigo, preco, quantidadeEstoque, estoqueMinimo
        );
        p1 = produtoRepository.save(p1);
        return p1;
    }

    @Test
    void deveDesfazerTodaVendaQuandoSegundoProdutoApresentarEstoqueInsuficiente() {

        //Preparar
        Cliente c1 = cadastrarClienteParaTeste();

        Produto p1 = cadastrarProdutoParaTeste(
                "Mouse", "PRD-001",
                new BigDecimal("49.90"), 5, 2
        );

        Produto p2 = cadastrarProdutoParaTeste(
                "Teclado", "PRD-002",
                new BigDecimal("89.90"), 1, 1
        );

        p1 = produtoRepository.save(p1);
        p2 = produtoRepository.save(p2);

        ItemVendaRequestDTO mouseVendaDTO = new ItemVendaRequestDTO(p1.getId(), 2);
        ItemVendaRequestDTO tecladoVendaDTO = new ItemVendaRequestDTO(p2.getId(), 2);
        List<ItemVendaRequestDTO> itensRequestDTO = new ArrayList<>();
        itensRequestDTO.add(mouseVendaDTO);
        itensRequestDTO.add(tecladoVendaDTO);

        VendaRequestDTO vendaRequestDTO = new VendaRequestDTO(c1.getId(), itensRequestDTO);

        //Executar e Verificar
        assertThrows(
                EstoqueInsuficiente.class,
                () -> vendaService.cadastrarVenda(vendaRequestDTO)
        );

        Produto mouseBanco = produtoRepository.findById(p1.getId()).orElseThrow();
        assertEquals(5, mouseBanco.getQuantidadeEstoque());

        Produto tecladoBanco = produtoRepository.findById(p2.getId()).orElseThrow();
        assertEquals(1, tecladoBanco.getQuantidadeEstoque());

        assertEquals(0, vendaRepository.count());
        assertEquals(0, itemVendaRepository.count());
        assertEquals(0, movimentacaoEstoqueRepository.count());

    }

    @Test
    void deveCadastrarVendaComDoisProdutosEatualizarEstoque() {

        //Preparar
        Cliente c1 = cadastrarClienteParaTeste();

        Produto p1 = cadastrarProdutoParaTeste(
                "Mouse", "PRD-001",
                new BigDecimal("49.90"), 5, 2
        );

        Produto p2 = cadastrarProdutoParaTeste(
                "Teclado", "PRD-002",
                new BigDecimal("89.90"), 1, 1
        );

        ItemVendaRequestDTO mouseVendaDTO = new ItemVendaRequestDTO(p1.getId(), 2);
        ItemVendaRequestDTO tecladoVendaDTO = new ItemVendaRequestDTO(p2.getId(), 1);
        List<ItemVendaRequestDTO> itensRequestDTO = new ArrayList<>();
        itensRequestDTO.add(mouseVendaDTO);
        itensRequestDTO.add(tecladoVendaDTO);

        VendaRequestDTO vendaRequestDTO = new VendaRequestDTO(c1.getId(), itensRequestDTO);

        //Executar
        Venda resultado = vendaService.cadastrarVenda(vendaRequestDTO);

        //Verificar
        assertEquals(1, vendaRepository.count());
        assertEquals(2, itemVendaRepository.count());
        assertEquals(2, movimentacaoEstoqueRepository.count());

        Venda vendaBanco = vendaRepository.findById(resultado.getId()).orElseThrow();
        assertEquals(StatusVenda.CONFIRMADA, vendaBanco.getStatusVenda());

        Produto mouseBanco = produtoRepository.findById(p1.getId()).orElseThrow();
        assertEquals(3, mouseBanco.getQuantidadeEstoque());

        Produto tecladoBanco = produtoRepository.findById(p2.getId()).orElseThrow();
        assertEquals(0, tecladoBanco.getQuantidadeEstoque());
    }

    @Test
    void deveCancelarVendaDevolvendoOestoqueDosProdutos() {

        //Preparar
        Cliente c1 = cadastrarClienteParaTeste();

        Produto p1 = cadastrarProdutoParaTeste(
                "Mouse", "PRD-001",
                new BigDecimal("49.90"), 5, 2
        );

        Produto p2 = cadastrarProdutoParaTeste(
                "Teclado", "PRD-002",
                new BigDecimal("89.90"), 1, 1
        );

        ItemVendaRequestDTO mouseVendaDTO = new ItemVendaRequestDTO(p1.getId(), 2);
        ItemVendaRequestDTO tecladoVendaDTO = new ItemVendaRequestDTO(p2.getId(), 1);
        List<ItemVendaRequestDTO> itensRequestDTO = new ArrayList<>();
        itensRequestDTO.add(mouseVendaDTO);
        itensRequestDTO.add(tecladoVendaDTO);

        VendaRequestDTO vendaRequestDTO = new VendaRequestDTO(c1.getId(), itensRequestDTO);

        Venda venda = vendaService.cadastrarVenda(vendaRequestDTO);

        //Executar
        vendaService.cancelarVenda(venda.getId());

        //Verificar
        Venda vendaBanco = vendaRepository.findById(venda.getId()).orElseThrow();
        assertEquals(StatusVenda.CANCELADA, vendaBanco.getStatusVenda());

        Produto mouseBanco = produtoRepository.findById(p1.getId()).orElseThrow();
        assertEquals(5, mouseBanco.getQuantidadeEstoque());

        Produto tecladoBanco = produtoRepository.findById(p2.getId()).orElseThrow();
        assertEquals(1, tecladoBanco.getQuantidadeEstoque());

        assertEquals(1, vendaRepository.count());
        assertEquals(2, itemVendaRepository.count());
        assertEquals(4, movimentacaoEstoqueRepository.count());

    }

    @Test
    void naoDeveDevolverEstoqueNovamenteComAvendaJaCancelada() {

        //Preparar
        Cliente c1 = cadastrarClienteParaTeste();

        Produto p1 = cadastrarProdutoParaTeste(
                "Mouse", "PRD-001",
                new BigDecimal("49.90"), 5, 2
        );

        Produto p2 = cadastrarProdutoParaTeste(
                "Teclado", "PRD-002",
                new BigDecimal("89.90"), 1, 1
        );

        ItemVendaRequestDTO mouseVendaDTO = new ItemVendaRequestDTO(p1.getId(), 2);
        ItemVendaRequestDTO tecladoVendaDTO = new ItemVendaRequestDTO(p2.getId(), 1);
        List<ItemVendaRequestDTO> itensRequestDTO = new ArrayList<>();
        itensRequestDTO.add(mouseVendaDTO);
        itensRequestDTO.add(tecladoVendaDTO);

        VendaRequestDTO vendaRequestDTO = new VendaRequestDTO(c1.getId(), itensRequestDTO);

        Venda venda = vendaService.cadastrarVenda(vendaRequestDTO);
        vendaService.cancelarVenda(venda.getId());

        //Executar
        vendaService.cancelarVenda(venda.getId());

        //Verificar
        Venda vendaBanco = vendaRepository.findById(venda.getId()).orElseThrow();
        assertEquals(StatusVenda.CANCELADA, vendaBanco.getStatusVenda());

        Produto mouseBanco = produtoRepository.findById(p1.getId()).orElseThrow();
        assertEquals(5, mouseBanco.getQuantidadeEstoque());

        Produto tecladoBanco = produtoRepository.findById(p2.getId()).orElseThrow();
        assertEquals(1, tecladoBanco.getQuantidadeEstoque());

        assertEquals(1, vendaRepository.count());
        assertEquals(2, itemVendaRepository.count());
        assertEquals(4, movimentacaoEstoqueRepository.count());

    }


}
