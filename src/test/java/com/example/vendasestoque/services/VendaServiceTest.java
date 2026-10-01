package com.example.vendasestoque.services;

import com.example.vendasestoque.dtos.ItemVendaRequestDTO;
import com.example.vendasestoque.dtos.VendaRequestDTO;
import com.example.vendasestoque.entities.*;
import com.example.vendasestoque.entities.PK.ItemVendaPK;
import com.example.vendasestoque.entities.enuns.StatusVenda;
import com.example.vendasestoque.entities.enuns.TipoMovimentacao;
import com.example.vendasestoque.repositories.ItemVendaRepository;
import com.example.vendasestoque.repositories.VendaRepository;
import com.example.vendasestoque.services.exceptions.EstoqueInsuficiente;
import com.example.vendasestoque.services.exceptions.ItemRepetidoNaCompra;
import com.example.vendasestoque.services.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VendaServiceTest {

    @Mock
    private VendaRepository vendaRepository;

    @Mock
    private ClienteService clienteService;

    @Mock
    private ProdutoService produtoService;

    @Mock
    private ItemVendaRepository itemVendaRepository;

    @Mock
    private MovimentacaoEstoqueService movimentacaoEstoqueService;

    @InjectMocks
    private VendaService vendaService;


    @Test
    void deveRetornarVendaJaCanceladaSemGerarNovasMovimentacoes() {

        //Preparar
        Venda v1 = new Venda(
                1L,
                Instant.parse("2026-09-21T14:00:00Z"),
                StatusVenda.CANCELADA, null
        );

        Produto p1 = new Produto(
                null, "Mouse", "PRD-001",
                new BigDecimal("49.90"), 10, 2
        );

        ItemVenda item1 = new ItemVenda(
                new ItemVendaPK(v1, p1), 2, p1.getPreco()
        );

        v1.getItens().add(item1);

        when(vendaRepository.findById(1L)).thenReturn(Optional.of(v1));

        //Executar
        Venda resultado = vendaService.cancelarVenda(1L);

        //Verificar
        assertEquals(StatusVenda.CANCELADA, v1.getStatusVenda());
        assertSame(v1, resultado);
        verify(movimentacaoEstoqueService, never()).cadastrarMovimentacao(any(MovimentacaoEstoque.class));
        verify(vendaRepository, never()).save(any());
    }

    @Test
    void deveCancelarVendaRegistrandoEntradaDeItem() {

        //Preparar
        Venda v1 = new Venda(
                1L,
                Instant.parse("2026-09-21T14:00:00Z"),
                StatusVenda.CONFIRMADA, null
        );

        Produto p1 = new Produto(
                1L, "Mouse", "PRD-001",
                new BigDecimal("49.90"), 10, 2
        );

        ItemVenda item1 = new ItemVenda(
                new ItemVendaPK(v1, p1), 2, p1.getPreco()
        );

        v1.getItens().add(item1);

        when(vendaRepository.findById(1L)).thenReturn(Optional.of(v1));

        //Executar
        vendaService.cancelarVenda(1L);

        //Verificar
        ArgumentCaptor<MovimentacaoEstoque> captor = ArgumentCaptor.forClass(MovimentacaoEstoque.class);
        verify(movimentacaoEstoqueService).cadastrarMovimentacao(captor.capture());
        MovimentacaoEstoque movimentacaoCapturada = captor.getValue();

        assertEquals(StatusVenda.CANCELADA, v1.getStatusVenda());
        assertEquals(TipoMovimentacao.ENTRADA, movimentacaoCapturada.getTipoMovimentacao());
        assertEquals(2, movimentacaoCapturada.getQuantidade());
        assertSame(p1, movimentacaoCapturada.getProduto());
        assertSame(v1, movimentacaoCapturada.getVenda());
        verify(vendaRepository).save(v1);

    }

    @Test
    void deveCancelarVendaGerandoUmaEntradaParaCadaItem() {

        //Preparar
        Venda v1 = new Venda(
                1L,
                Instant.parse("2026-09-21T14:00:00Z"),
                StatusVenda.CONFIRMADA, null
        );

        Produto p1 = new Produto(
                1L, "Mouse", "PRD-001",
                new BigDecimal("49.90"), 10, 2
        );

        Produto p2 = new Produto(
                2L, "Teclado", "PRD-002",
                new BigDecimal("89.90"), 5, 1
        );

        ItemVenda item1 = new ItemVenda(
                new ItemVendaPK(v1, p1), 2, p1.getPreco()
        );

        ItemVenda item2 = new ItemVenda(
                new ItemVendaPK(v1, p2), 1, p2.getPreco()
        );

        v1.getItens().add(item1);
        v1.getItens().add(item2);

        when(vendaRepository.findById(1L)).thenReturn(Optional.of(v1));

        //Executar
        vendaService.cancelarVenda(1L);

        //Verificar
        ArgumentCaptor<MovimentacaoEstoque> captor = ArgumentCaptor.forClass(MovimentacaoEstoque.class);
        verify(movimentacaoEstoqueService, times(2))
                .cadastrarMovimentacao(captor.capture());
        List<MovimentacaoEstoque> list = captor.getAllValues();

        MovimentacaoEstoque entradaMouse = null;

        for(MovimentacaoEstoque mov : list) {
            if(mov.getProduto().getId().equals(p1.getId())) {
                entradaMouse = mov;
                break;
            }
        }

        assertEquals(StatusVenda.CANCELADA, v1.getStatusVenda());
        assertNotNull(entradaMouse);
        assertEquals(2, entradaMouse.getQuantidade());
        assertEquals(TipoMovimentacao.ENTRADA, entradaMouse.getTipoMovimentacao());
        assertSame(v1, entradaMouse.getVenda());

        MovimentacaoEstoque entradaTeclado = null;

        for(MovimentacaoEstoque mov : list) {
            if(mov.getProduto().getId().equals(p2.getId())) {
                entradaTeclado = mov;
                break;
            }
        }

        assertNotNull(entradaTeclado);
        assertEquals(1, entradaTeclado.getQuantidade());
        assertEquals(TipoMovimentacao.ENTRADA, entradaTeclado.getTipoMovimentacao());
        assertSame(v1, entradaTeclado.getVenda());
    }

    @Test
    void deveLancarExcecaoDeVendaNaoExistente() {

        //Preparar
        when(vendaRepository.findById(99L)).thenReturn(Optional.empty());

        //Executar e Verificar
        assertThrows(
                ResourceNotFoundException.class,
                () -> vendaService.cancelarVenda(99L)
        );

        verify(movimentacaoEstoqueService, never()).cadastrarMovimentacao(any(MovimentacaoEstoque.class));
        verify(vendaRepository, never()).save(any());
    }

    @Test
    void deveCadastrarVendaComUmItem() {

        //Preparar
        Cliente c1 = new Cliente(
                1L, "Maria Silva",
                "maria@example.com", "11987654321"
        );

        Produto p1 = new Produto(
                1L, "Mouse", "PRD-001",
                new BigDecimal("49.90"), 10, 2
        );

        ItemVendaRequestDTO itemRequestDTO = new ItemVendaRequestDTO(p1.getId(), 2);
        List<ItemVendaRequestDTO> list = new ArrayList<>();
        list.add(itemRequestDTO);

        VendaRequestDTO vendaRequestDTO = new VendaRequestDTO(c1.getId(), list);

        when(clienteService.buscarClientePorId(1L)).thenReturn(c1);
        when(produtoService.buscarProdutoPorId(1L)).thenReturn(p1);
        when(vendaRepository.save(any(Venda.class))).thenAnswer(chamada -> chamada.getArgument(0));
        when(itemVendaRepository.save(any(ItemVenda.class))).thenAnswer(chamada -> chamada.getArgument(0));

        //Executar
        Venda resultado = vendaService.cadastrarVenda(vendaRequestDTO);

        //Verificar
        ArgumentCaptor<Venda> captor = ArgumentCaptor.forClass(Venda.class);
        verify(vendaRepository).save(captor.capture());
        Venda vendaCapturada = captor.getValue();

        ArgumentCaptor<ItemVenda> captor1 = ArgumentCaptor.forClass(ItemVenda.class);
        verify(itemVendaRepository).save(captor1.capture());
        ItemVenda itemVendaCapturado = captor1.getValue();

        ArgumentCaptor<MovimentacaoEstoque> captor2 = ArgumentCaptor.forClass(MovimentacaoEstoque.class);
        verify(movimentacaoEstoqueService).cadastrarMovimentacao(captor2.capture());
        MovimentacaoEstoque movimentacaoEstoqueCapturada = captor2.getValue();

        //Executar
        assertEquals(StatusVenda.CONFIRMADA, vendaCapturada.getStatusVenda());
        assertSame(c1, vendaCapturada.getCliente());
        assertEquals(1, vendaCapturada.getItens().size());
        assertSame(vendaCapturada, resultado);

        assertSame(itemVendaCapturado.getId().getVenda(), vendaCapturada);
        assertSame(p1, itemVendaCapturado.getId().getProduto());
        assertEquals(2, itemVendaCapturado.getQuantidade());
        assertEquals(new BigDecimal("49.90"), itemVendaCapturado.getPrecoUnitario());

        assertEquals(TipoMovimentacao.SAIDA, movimentacaoEstoqueCapturada.getTipoMovimentacao());
        assertEquals(2, movimentacaoEstoqueCapturada.getQuantidade());
        assertSame(p1, movimentacaoEstoqueCapturada.getProduto());
        assertSame(vendaCapturada, movimentacaoEstoqueCapturada.getVenda());
    }

    @Test
    void deveRejeitarVendaComProdutoRepetido() {

        //Preparar
        Cliente c1 = new Cliente(
                1L, "Maria Silva",
                "maria@example.com", "11987654321"
        );

        Produto p1 = new Produto(
                1L, "Mouse", "PRD-001",
                new BigDecimal("49.90"), 10, 2
        );
        ItemVendaRequestDTO itemVendaRequestDTO = new ItemVendaRequestDTO(p1.getId(), 2);
        ItemVendaRequestDTO itemVendaRequestDTO1 = new ItemVendaRequestDTO(p1.getId(), 1);
        List<ItemVendaRequestDTO> list = new ArrayList<>();
        list.add(itemVendaRequestDTO);
        list.add(itemVendaRequestDTO1);

        VendaRequestDTO vendaRequestDTO = new VendaRequestDTO(c1.getId(), list);


        when(clienteService.buscarClientePorId(1L)).thenReturn(c1);
        when(produtoService.buscarProdutoPorId(1L)).thenReturn(p1);
        when(vendaRepository.save(any(Venda.class))).thenAnswer(chamada -> chamada.getArgument(0));
        when(itemVendaRepository.save(any(ItemVenda.class))).thenAnswer(chamada -> chamada.getArgument(0));

        //Executar e Verificar
        assertThrows(
                ItemRepetidoNaCompra.class,
                () -> vendaService.cadastrarVenda(vendaRequestDTO)
        );

        verify(vendaRepository).save(any(Venda.class));
        verify(itemVendaRepository).save(any(ItemVenda.class));
        verify(movimentacaoEstoqueService).cadastrarMovimentacao(any(MovimentacaoEstoque.class));

    }

    @Test
    void deveRejeitarVendaQuandoClienteNaoExistir() {

        //Preparar
        Produto p1 = new Produto(
                1L, "Mouse", "PRD-001",
                new BigDecimal("49.90"), 10, 2
        );

        ItemVendaRequestDTO itemVendaRequestDTO = new ItemVendaRequestDTO(p1.getId(), 2);
        List<ItemVendaRequestDTO> list = new ArrayList<>();
        list.add(itemVendaRequestDTO);

        VendaRequestDTO vendaRequestDTO = new VendaRequestDTO(99L, list);

        //Executar e Verificar
        when(clienteService.buscarClientePorId(99L)).thenThrow(new ResourceNotFoundException(99L));

        assertThrows(
                ResourceNotFoundException.class,
                () -> vendaService.cadastrarVenda(vendaRequestDTO)
        );

        verify(produtoService, never()).buscarProdutoPorId(any());
        verify(vendaRepository, never()).save(any());
        verify(itemVendaRepository, never()).save(any());
        verify(movimentacaoEstoqueService, never()).cadastrarMovimentacao(any(MovimentacaoEstoque.class));
    }

    @Test
    void deveRejeitarVendaQuandoProdutoNaoExistir() {

        //Preparar
        Cliente c1 = new Cliente(
                1L, "Maria Silva",
                "maria@example.com", "11987654321"
        );

        ItemVendaRequestDTO itemVendaRequestDTO = new ItemVendaRequestDTO(99L, 2);
        List<ItemVendaRequestDTO> list = new ArrayList<>();
        list.add(itemVendaRequestDTO);

        VendaRequestDTO vendaRequestDTO = new VendaRequestDTO(c1.getId(), list);

        when(clienteService.buscarClientePorId(1L)).thenReturn(c1);
        when(vendaRepository.save(any(Venda.class))).thenAnswer(chamada -> chamada.getArgument(0));
        when(produtoService.buscarProdutoPorId(99L)).thenThrow(new ResourceNotFoundException(99L));

        //Executar e Verificar
        assertThrows(
                ResourceNotFoundException.class,
                () -> vendaService.cadastrarVenda(vendaRequestDTO)
        );

        verify(vendaRepository).save(any(Venda.class));
        verify(itemVendaRepository, never()).save(any());
        verify(movimentacaoEstoqueService, never()).cadastrarMovimentacao(any(MovimentacaoEstoque.class));

    }

    @Test
    void deveRejeitarVendaQuandoEstoqueForInsuficiente() {

        //Preparar
        Cliente c1 = new Cliente(
                1L, "Maria Silva",
                "maria@example.com", "11987654321"
        );

        Produto p1 = new Produto(
                1L, "Mouse", "PRD-001",
                new BigDecimal("49.90"), 1, 2
        );

        ItemVendaRequestDTO itemVendaRequestDTO = new ItemVendaRequestDTO(p1.getId(), 2);
        List<ItemVendaRequestDTO> list = new ArrayList<>();
        list.add(itemVendaRequestDTO);

        VendaRequestDTO vendaRequestDTO = new VendaRequestDTO(c1.getId(), list);

        when(clienteService.buscarClientePorId(1L)).thenReturn(c1);
        when(vendaRepository.save(any(Venda.class))).thenAnswer(chamada -> chamada.getArgument(0));
        when(produtoService.buscarProdutoPorId(1L)).thenReturn(p1);
        when(itemVendaRepository.save(any(ItemVenda.class))).thenAnswer(chamada -> chamada.getArgument(0));
        when(movimentacaoEstoqueService.cadastrarMovimentacao(any(MovimentacaoEstoque.class))).thenThrow(new EstoqueInsuficiente("Estoque insuficiente"));

        assertThrows(
                EstoqueInsuficiente.class,
                () -> vendaService.cadastrarVenda(vendaRequestDTO)
        );

        verify(vendaRepository).save(any(Venda.class));
        verify(itemVendaRepository).save(any(ItemVenda.class));
        verify(movimentacaoEstoqueService).cadastrarMovimentacao(any(MovimentacaoEstoque.class));

    }

    @Test
    void deveCadastrarVendaComDoisProdutosDistintos() {

        //Preparar
        Cliente c1 = new Cliente(
                1L, "Maria Silva",
                "maria@example.com", "11987654321"
        );

        Produto p1 = new Produto(
                1L, "Mouse", "PRD-001",
                new BigDecimal("49.90"), 10, 2
        );

        Produto p2 = new Produto(
                2L, "Teclado", "PRD-002",
                new BigDecimal("109.90"), 10, 2
        );

        ItemVendaRequestDTO itemRequestDTO = new ItemVendaRequestDTO(p1.getId(), 2);
        ItemVendaRequestDTO itemRequestDTO1 = new ItemVendaRequestDTO(p2.getId(), 2);
        List<ItemVendaRequestDTO> list = new ArrayList<>();
        list.add(itemRequestDTO);
        list.add(itemRequestDTO1);

        VendaRequestDTO vendaRequestDTO = new VendaRequestDTO(c1.getId(), list);

        when(clienteService.buscarClientePorId(1L)).thenReturn(c1);
        when(vendaRepository.save(any(Venda.class))).thenAnswer(chamada -> chamada.getArgument(0));
        when(produtoService.buscarProdutoPorId(1L)).thenReturn(p1);
        when(produtoService.buscarProdutoPorId(2L)).thenReturn(p2);
        when(itemVendaRepository.save(any(ItemVenda.class))).thenAnswer(chamada -> chamada.getArgument(0));

        //Executar
        Venda resultado = vendaService.cadastrarVenda(vendaRequestDTO);

        //Verificar
        ArgumentCaptor<Venda> captorVenda = ArgumentCaptor.forClass(Venda.class);
        verify(vendaRepository).save(captorVenda.capture());
        Venda vendaCapturada = captorVenda.getValue();

        ArgumentCaptor<ItemVenda> captorItens = ArgumentCaptor.forClass(ItemVenda.class);
        verify(itemVendaRepository, times(2)).save(captorItens.capture());
        List<ItemVenda> itensCapturados = captorItens.getAllValues();

        ItemVenda itemVendaMouse = null;

        for(ItemVenda itemVenda : itensCapturados) {
            if(itemVenda.getId().getProduto().getId().equals(p1.getId())) {
                itemVendaMouse = itemVenda;
                break;
            }
        }

        ItemVenda itemVendaTeclado = null;

        for(ItemVenda itemVenda : itensCapturados) {
            if(itemVenda.getId().getProduto().getId().equals(p2.getId())) {
                itemVendaTeclado = itemVenda;
                break;
            }
        }

        assertSame(vendaCapturada, resultado);
        assertEquals(StatusVenda.CONFIRMADA, vendaCapturada.getStatusVenda());
        assertSame(c1, vendaCapturada.getCliente());
        assertEquals(2, vendaCapturada.getItens().size());

        assertNotNull(itemVendaMouse);
        assertSame(itemVendaMouse.getId().getVenda(), vendaCapturada);
        assertSame(p1, itemVendaMouse.getId().getProduto());
        assertEquals(2, itemVendaMouse.getQuantidade());
        assertEquals(new BigDecimal("49.90"), itemVendaMouse.getPrecoUnitario());

        assertNotNull(itemVendaTeclado);
        assertSame(itemVendaTeclado.getId().getVenda(), vendaCapturada);
        assertSame(p2, itemVendaTeclado.getId().getProduto());
        assertEquals(2, itemVendaTeclado.getQuantidade());
        assertEquals(new BigDecimal("109.90"), itemVendaTeclado.getPrecoUnitario());

        ArgumentCaptor<MovimentacaoEstoque> captorMovimentacoes = ArgumentCaptor.forClass(MovimentacaoEstoque.class);
        verify(movimentacaoEstoqueService, times(2)).cadastrarMovimentacao(captorMovimentacoes.capture());
        List<MovimentacaoEstoque> movimentacoes = captorMovimentacoes.getAllValues();

        MovimentacaoEstoque movimentacaoMouse = null;

        for(MovimentacaoEstoque mov : movimentacoes) {
            if(mov.getProduto().getId().equals(p1.getId())) {
                movimentacaoMouse = mov;
                break;
            }
        }

        MovimentacaoEstoque movimentacaoTeclado = null;

        for(MovimentacaoEstoque mov : movimentacoes) {
            if(mov.getProduto().getId().equals(p2.getId())) {
                movimentacaoTeclado = mov;
                break;
            }
        }

        assertNotNull(movimentacaoMouse);
        assertEquals(TipoMovimentacao.SAIDA, movimentacaoMouse.getTipoMovimentacao());
        assertEquals(2, movimentacaoMouse.getQuantidade());
        assertSame(p1, movimentacaoMouse.getProduto());
        assertSame(vendaCapturada, movimentacaoMouse.getVenda());

        assertNotNull(movimentacaoTeclado);
        assertEquals(TipoMovimentacao.SAIDA, movimentacaoTeclado.getTipoMovimentacao());
        assertEquals(2, movimentacaoTeclado.getQuantidade());
        assertSame(p2, movimentacaoTeclado.getProduto());
        assertSame(vendaCapturada, movimentacaoTeclado.getVenda());
    }
}
