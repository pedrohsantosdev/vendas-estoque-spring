package com.example.vendasestoque.services;

import com.example.vendasestoque.dtos.ProdutoRequestDTO;
import com.example.vendasestoque.entities.Produto;
import com.example.vendasestoque.repositories.ProdutoRepository;
import com.example.vendasestoque.services.ProdutoService;
import com.example.vendasestoque.services.exceptions.CodigoExistente;
import com.example.vendasestoque.services.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProdutoServiceTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @InjectMocks
    private ProdutoService produtoService;

    @Test
    void deveRetornarProdutoQuandoIdExistir() {

        //Preparar
        Produto p1 = new Produto(
                1L, "Mouse", "PRD-001",
                new BigDecimal("49.90"), 5, 2
        );

        when(produtoRepository.findById(1L)).thenReturn(Optional.of(p1));

        //Executar
        Produto produto = produtoService.buscarProdutoPorId(1L);

        //Verificar
        assertSame(p1, produto);

    }

    @Test
    void deveLancarExcecaoQuandoProdutoNaoExistir() {

        //Preparar
        when(produtoRepository.findById(3L)).thenReturn(Optional.empty());

        //Executar e Verificar
        assertThrows(
                ResourceNotFoundException.class,
                () -> produtoService.buscarProdutoPorId(3L)
        );
    }

    @Test
    void deveRejeitarAtualizacaoQuandoCodigoPertenceOutroProduto() {

        //Preparar
        Produto produtoAtual = new Produto(
                1L, "Mouse", "PRD-001",
                new BigDecimal("49.90"), 5, 2
        );

        Produto p2 = new Produto(
                2L, "Teclado Magnético", "PRD-002",
                new BigDecimal("259.90"), 5, 2
        );

        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produtoAtual));
        when(produtoRepository.findByCodigo("PRD-002")).thenReturn(p2);

        //Executar e Verificar
        assertThrows(
                CodigoExistente.class,
                () -> produtoService.atualizarProduto(1L, new ProdutoRequestDTO(produtoAtual.getNome(),
                        "PRD-002", produtoAtual.getPreco(), produtoAtual.getEstoqueMinimo()))
        );

        assertEquals("PRD-001", produtoAtual.getCodigo());
        verify(produtoRepository, never()).save(any());

    }

    @Test
    void devePermitirAtualizacaoMatendoCodigoDoPropioProduto() {

        //Preparar
        Produto produtoAtual = new Produto(
                1L, "Mouse", "PRD-001",
                new BigDecimal("49.90"), 5, 2
        );

        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produtoAtual));
        when(produtoRepository.findByCodigo("PRD-001")).thenReturn(produtoAtual);

        //Executar
        produtoService.atualizarProduto(1L, new ProdutoRequestDTO("Mouse sem fio",
                produtoAtual.getCodigo(), produtoAtual.getPreco(), produtoAtual.getEstoqueMinimo()));

        //Verificar
        assertEquals("Mouse sem fio", produtoAtual.getNome());
        assertEquals("PRD-001", produtoAtual.getCodigo());
        verify(produtoRepository).save(produtoAtual);

    }

    @Test
    void devePermitirAtualizacaoParaCodigoDisponivel() {

        //Preparar
        Produto produtoAtual = new Produto(
                1L, "Mouse", "PRD-001",
                new BigDecimal("49.90"), 5, 2
        );

        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produtoAtual));
        when(produtoRepository.findByCodigo("PRD-003")).thenReturn(null);

        //Executar
        produtoService.atualizarProduto(1L, new ProdutoRequestDTO(produtoAtual.getNome(),
                "PRD-003", produtoAtual.getPreco(), produtoAtual.getEstoqueMinimo()));

        //Verificar
        assertEquals("PRD-003", produtoAtual.getCodigo());
        verify(produtoRepository).save(produtoAtual);
    }

    @Test
    void deveRejeitarAtualizacaoQuandoOprodutoNaoExistir() {

        Produto produtoAtual = new Produto(
                1L, "Mouse", "PRD-001",
                new BigDecimal("49.90"), 5, 2
        );

        when(produtoRepository.findById(99L)).thenReturn(Optional.empty());

        //Executar e Verificar
        assertThrows(
                ResourceNotFoundException.class,
                () -> produtoService.atualizarProduto(99L, new ProdutoRequestDTO(produtoAtual.getNome(),
                        "PRD-003", produtoAtual.getPreco(), produtoAtual.getEstoqueMinimo()))
        );


        verify(produtoRepository, never()).findByCodigo(anyString());
        verify(produtoRepository, never()).save(any());
    }

    @Test
    void deveSolicitarExclusaoQuandoProdutoExistir() {

        //Preparar
        Produto p1 = new Produto(
                1L, "Mouse", "PRD-001",
                new BigDecimal("49.90"), 5, 2
        );

        when(produtoRepository.findById(1L)).thenReturn(Optional.of(p1));

        //Executar
        produtoService.deletarProduto(1L);

        //Verificar
        verify(produtoRepository).delete(p1);
    }

    @Test
    void deveRejeitarExclusaoQuandoProdutoNaoExistir() {

        //Preparar

        when(produtoRepository.findById(99L)).thenReturn(Optional.empty());

        //Executar e Verificar
        assertThrows(
                ResourceNotFoundException.class,
                () -> produtoService.deletarProduto(99L)
        );

        verify(produtoRepository, never()).delete(any());
    }

    @Test
    void deveRejeitarCadastroQuandoCodigoJaExiste() {

        //Preparar
        ProdutoRequestDTO requestDTO = new ProdutoRequestDTO("Mouse", "PRD-001",
                new BigDecimal("49.90"), 2);

        when(produtoRepository.existsByCodigo(requestDTO.codigo())).thenReturn(true);

        //Executar e Verificar
        assertThrows(
                CodigoExistente.class,
                () -> produtoService.cadastrarProduto(requestDTO)
        );

        verify(produtoRepository, never()).save(any());
    }

    @Test
    void deveCadastrarProdutoComEstoqueZeroQuandoCodigoEstiverDisponivel() {

        //Preparar
        ProdutoRequestDTO requestDTO = new ProdutoRequestDTO("Mouse", "PRD-001",
                new BigDecimal("49.90"), 2);

        when(produtoRepository.existsByCodigo(requestDTO.codigo())).thenReturn(false);

        //Executar
        produtoService.cadastrarProduto(requestDTO);

        //Verificar
        ArgumentCaptor<Produto> captor = ArgumentCaptor.forClass(Produto.class);

        verify(produtoRepository).save(captor.capture());

        Produto produtoCapturado = captor.getValue();

        assertEquals("Mouse", produtoCapturado.getNome());
        assertEquals("PRD-001", produtoCapturado.getCodigo());
        assertEquals(new BigDecimal("49.90"), produtoCapturado.getPreco());
        assertEquals(0, produtoCapturado.getQuantidadeEstoque());
        assertEquals(2, produtoCapturado.getEstoqueMinimo());

    }



}
