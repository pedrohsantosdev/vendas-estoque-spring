package com.example.vendasestoque.services;

import com.example.vendasestoque.entities.MovimentacaoEstoque;
import com.example.vendasestoque.entities.Produto;
import com.example.vendasestoque.entities.enuns.TipoMovimentacao;
import com.example.vendasestoque.repositories.MovimentacaoEstoqueRepository;
import com.example.vendasestoque.services.MovimentacaoEstoqueService;
import com.example.vendasestoque.services.ProdutoService;
import com.example.vendasestoque.services.exceptions.EstoqueInsuficiente;
import com.example.vendasestoque.services.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MovimentacaoEstoqueServiceTest {

    @Mock
    private MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;

    @Mock
    private ProdutoService produtoService;

    @InjectMocks
    private MovimentacaoEstoqueService movimentacaoEstoqueService;

    @Test
    void deveLancarExecaoQuandoEstoqueForInsuficiente() {

        //Preparar
        Produto p1 = new Produto(
                1L, "Mouse", "PRD-001",
                new BigDecimal("49.90"), 5, 2
        );

        MovimentacaoEstoque saida = new MovimentacaoEstoque(
                null,
                TipoMovimentacao.SAIDA,
                6,
                Instant.now(),
                "Saída para teste",
                p1,
                null
        );

        when(produtoService.buscarProdutoPorId(1L)).thenReturn(p1);

        // Executar e verificar
        assertThrows(
                EstoqueInsuficiente.class,
                () -> movimentacaoEstoqueService.cadastrarMovimentacao(saida)
        );

        assertEquals(5, p1.getQuantidadeEstoque());
        verify(movimentacaoEstoqueRepository, never()).save(any());

    }

    @Test
    void devePermitirSaidaDeTodoEstoqueDisponivel() {

        //Preparar
        Produto p1 = new Produto(
                1L, "Mouse", "PRD-001",
                new BigDecimal("49.90"), 5, 2
        );

        MovimentacaoEstoque saida = new MovimentacaoEstoque(
                null,
                TipoMovimentacao.SAIDA,
                5,
                Instant.now(),
                "Saída para teste",
                p1,
                null
        );

        when(produtoService.buscarProdutoPorId(1L)).thenReturn(p1);

        //Executar
        movimentacaoEstoqueService.cadastrarMovimentacao(saida);

        //Verificar
        assertEquals(0, p1.getQuantidadeEstoque());
        verify(movimentacaoEstoqueRepository).save(saida);

    }

    @Test
    void deveRejeitarMovimentacaoComQuantidadeZero() {

        //Preparar
        Produto p1 = new Produto(
                1L, "Mouse", "PRD-001",
                new BigDecimal("49.90"), 5, 2
        );

        MovimentacaoEstoque saida = new MovimentacaoEstoque(
                null,
                TipoMovimentacao.SAIDA,
                0,
                Instant.now(),
                "Saída para teste",
                p1,
                null
        );

        when(produtoService.buscarProdutoPorId(1L)).thenReturn(p1);

        //Verificar e Executar
        assertThrows(
                IllegalArgumentException.class,
                () -> movimentacaoEstoqueService.cadastrarMovimentacao(saida)
        );

        assertEquals(5, p1.getQuantidadeEstoque());
        verify(movimentacaoEstoqueRepository, never()).save(any());
    }

    @Test
    void deveRejeitarMovimentacaoComQuantidadeNegativa() {

        //Preparar
        Produto p1 = new Produto(
                1L, "Mouse", "PRD-001",
                new BigDecimal("49.90"), 5, 2
        );

        MovimentacaoEstoque saida = new MovimentacaoEstoque(
                null,
                TipoMovimentacao.SAIDA,
                -1,
                Instant.now(),
                "Saída para teste",
                p1,
                null
        );

        when(produtoService.buscarProdutoPorId(1L)).thenReturn(p1);

        //Executar e Verificar
        assertThrows(
                IllegalArgumentException.class,
                () -> movimentacaoEstoqueService.cadastrarMovimentacao(saida)
        );

        assertEquals(5, p1.getQuantidadeEstoque());
        verify(movimentacaoEstoqueRepository, never()).save(any());

    }

    @Test
    void deveRejeitarMovimentacaoComQuantidadeNula() {

        //Preparar
        Produto p1 = new Produto(
                1L, "Mouse", "PRD-001",
                new BigDecimal("49.90"), 5, 2
        );

        MovimentacaoEstoque saida = new MovimentacaoEstoque(
                null,
                TipoMovimentacao.SAIDA,
                null,
                Instant.now(),
                "Saída para teste",
                p1,
                null
        );

        when(produtoService.buscarProdutoPorId(1L)).thenReturn(p1);

        //Executar e Verificar
        assertThrows(
                IllegalArgumentException.class,
                () -> movimentacaoEstoqueService.cadastrarMovimentacao(saida)
        );

        assertEquals(5, p1.getQuantidadeEstoque());
        verify(movimentacaoEstoqueRepository, never()).save(any());
    }

    @Test
    void deveAumentarEstoqueAoCadastrarEntrada() {

        //Preparar
        Produto p1 = new Produto(
                1L, "Mouse", "PRD-001",
                new BigDecimal("49.90"), 5, 2
        );

        MovimentacaoEstoque entrada = new MovimentacaoEstoque(
                null,
                TipoMovimentacao.ENTRADA,
                3,
                Instant.now(),
                "Entrada para teste",
                p1,
                null
        );

        when(produtoService.buscarProdutoPorId(1L)).thenReturn(p1);
        when(movimentacaoEstoqueRepository.save(entrada)).thenReturn(entrada);

        //Executar
        MovimentacaoEstoque resultado = movimentacaoEstoqueService.cadastrarMovimentacao(entrada);

        //Verificar
        assertEquals(8, p1.getQuantidadeEstoque());
        verify(movimentacaoEstoqueRepository).save(entrada);
        assertSame(entrada, resultado);
    }

    @Test
    void deveValidarSaidaUsandoEstoqueDoProdutoBuscado() {

        //Preparar
        Produto produtoRecebido = new Produto(
                1L, "Mouse", "PRD-001",
                new BigDecimal("49.90"), 100, 2
        );

        Produto produtoBuscado = new Produto(
                1L, "Teclado Magnético", "PRD-001",
                new BigDecimal("259.90"), 5, 2
        );

        MovimentacaoEstoque saida = new MovimentacaoEstoque(
                null,
                TipoMovimentacao.SAIDA,
                6,
                Instant.now(),
                "Saída para teste",
                produtoBuscado,
                null
        );

        when(produtoService.buscarProdutoPorId(1L)).thenReturn(produtoBuscado);

        //Executar e Verificar

        assertThrows(
                EstoqueInsuficiente.class,
                () -> movimentacaoEstoqueService.cadastrarMovimentacao(saida)
        );

        assertEquals(5, produtoBuscado.getQuantidadeEstoque());
        verify(movimentacaoEstoqueRepository, never()).save(any());

    }

    @Test
    void deveRejeitarMovimentacaoQuandoProdutoNaoExistir() {

        //Preparar
        Produto p1 = new Produto(
                99L, "Teclado Magnético", "PRD-001",
                new BigDecimal("259.90"), 5, 2
        );

        MovimentacaoEstoque saida = new MovimentacaoEstoque(
                null,
                TipoMovimentacao.SAIDA,
                3,
                Instant.now(),
                "Saída para teste",
                p1,
                null
        );

        when(produtoService.buscarProdutoPorId(99L)).thenThrow(new ResourceNotFoundException(99L));

        //Executar e Verificar
        assertThrows(
                ResourceNotFoundException.class,
                () -> movimentacaoEstoqueService.cadastrarMovimentacao(saida)
        );

        assertEquals(5, p1.getQuantidadeEstoque());
        verify(movimentacaoEstoqueRepository, never()).save(any());
    }
}
