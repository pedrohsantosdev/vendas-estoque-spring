package com.example.vendasestoque.services;

import com.example.vendasestoque.dtos.MovimentacaoRequestDTO;
import com.example.vendasestoque.dtos.MovimentacaoUpdateDTO;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

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
    void deveCadastrarMovimentacao() {

        //Preparar
        Produto p1 = new Produto(
                1L, "Mouse", "PRD-001",
                new BigDecimal("49.90"), 10, 2
        );

        MovimentacaoRequestDTO movimentacaoRequestDTO = new MovimentacaoRequestDTO(
                p1.getId(), TipoMovimentacao.SAIDA, 5, "Venda do Produto"
        );

        when(produtoService.buscarProdutoPorId(1L)).thenReturn(p1);

        //Executar
        movimentacaoEstoqueService.cadastrarMovimentacao(movimentacaoRequestDTO);

        //Verificar o estoque
        assertEquals(5, p1.getQuantidadeEstoque());

        //Capturar o entidade enviada ao repositório
        ArgumentCaptor<MovimentacaoEstoque> captor = ArgumentCaptor.forClass(MovimentacaoEstoque.class);
        verify(movimentacaoEstoqueRepository).save(captor.capture());
        MovimentacaoEstoque movimentacaoCapturada = captor.getValue();

        //Verificar a conversão de DTO em entidade
        assertEquals(TipoMovimentacao.SAIDA, movimentacaoCapturada.getTipoMovimentacao());
        assertEquals(5, movimentacaoCapturada.getQuantidade());
        assertEquals("Venda do Produto", movimentacaoCapturada.getMotivo());
        assertSame(p1, movimentacaoCapturada.getProduto());
        assertNull(movimentacaoCapturada.getVenda());
        assertNotNull(movimentacaoCapturada.getMomento());

    }

    @Test
    void deveAtualizarMotivo() {

        //Preparar
        Produto p1 = new Produto(
                1L, "Mouse", "PRD-001",
                new BigDecimal("49.90"), 10, 2
        );

        MovimentacaoEstoque saida = new MovimentacaoEstoque(
                1L,
                TipoMovimentacao.SAIDA,
                2,
                Instant.now(),
                "Saída para teste",
                p1,
                null
        );

        MovimentacaoUpdateDTO updateDTO = new MovimentacaoUpdateDTO("Venda de produto");

        when(movimentacaoEstoqueRepository.findById(1L)).thenReturn(Optional.of(saida));

        //Executar
        movimentacaoEstoqueService.atualizarMovimentacao(1L, updateDTO);

        //Verificar
        assertEquals(10, p1.getQuantidadeEstoque());
        assertEquals(1L, saida.getId());
        assertEquals(TipoMovimentacao.SAIDA, saida.getTipoMovimentacao());
        assertEquals(2, saida.getQuantidade());
        assertNotNull(saida.getMomento());
        assertEquals(updateDTO.motivo(), saida.getMotivo());
        assertSame(p1, saida.getProduto());
        assertNull(saida.getVenda());
        verify(movimentacaoEstoqueRepository).save(saida);

    }

    @Test
    void deveLancarExcecaoAoAtualizarMovimentacaoComIdInexistente() {

        //Preparar
        MovimentacaoUpdateDTO updateDTO = new MovimentacaoUpdateDTO("Venda de produto");

        when(movimentacaoEstoqueRepository.findById(99L)).thenReturn(Optional.empty());

        //Executar e Verificar
        assertThrows(
                ResourceNotFoundException.class,
                () -> movimentacaoEstoqueService.atualizarMovimentacao(99L, updateDTO)
        );

        verify(movimentacaoEstoqueRepository, never()).save(any(MovimentacaoEstoque.class));

    }

    @Test
    void deveRetornarMovimentacaoComIdExistente() {

        //Preparar
        Produto p1 = new Produto(
                1L, "Mouse", "PRD-001",
                new BigDecimal("49.90"), 10, 2
        );

        MovimentacaoEstoque saida = new MovimentacaoEstoque(
                1L,
                TipoMovimentacao.SAIDA,
                2,
                Instant.now(),
                "Saída para teste",
                p1,
                null
        );

        when(movimentacaoEstoqueRepository.findById(1L)).thenReturn(Optional.of(saida));

        //Executar
        MovimentacaoEstoque resultado = movimentacaoEstoqueService.buscarMovimentacaoPorId(1L);

        //Verificar
        assertSame(p1, resultado.getProduto());
        assertEquals(1L, resultado.getId());
        assertEquals(TipoMovimentacao.SAIDA, resultado.getTipoMovimentacao());
        assertEquals("Saída para teste", resultado.getMotivo());
        assertNull(resultado.getVenda());
        verify(movimentacaoEstoqueRepository).findById(1L);

    }

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
        verify(movimentacaoEstoqueRepository, never()).save(any(MovimentacaoEstoque.class));

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

        // Preparar: produto recebido informa estoque de 100.
        Produto produtoRecebido = new Produto(
                1L, "Teclado Magnético", "PRD-001",
                new BigDecimal("259.90"), 100, 2
        );

        // A consulta retorna o mesmo ID, mas com estoque de apenas 5.
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
                produtoRecebido,
                null
        );

        when(produtoService.buscarProdutoPorId(1L))
                .thenReturn(produtoBuscado);

        // Executar e verificar: o estoque consultado não permite retirar 6.
        assertThrows(
                EstoqueInsuficiente.class,
                () -> movimentacaoEstoqueService.cadastrarMovimentacao(saida)
        );

        assertEquals(5, produtoBuscado.getQuantidadeEstoque());
        assertEquals(100, produtoRecebido.getQuantidadeEstoque());

        verify(produtoService).buscarProdutoPorId(1L);
        verify(movimentacaoEstoqueRepository, never())
                .save(any(MovimentacaoEstoque.class));
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
