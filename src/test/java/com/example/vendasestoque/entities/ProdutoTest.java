package com.example.vendasestoque.entities;

import com.example.vendasestoque.entities.ItemVenda;
import com.example.vendasestoque.entities.PK.ItemVendaPK;
import com.example.vendasestoque.entities.Produto;
import com.example.vendasestoque.entities.Venda;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class ProdutoTest {

    @Test
    void deveIndicarEstoqueBaixoQuandoForAbaixoQueMinimo() {

        //Preparar
        Produto produto = new Produto();
        produto.setQuantidadeEstoque(3);
        produto.setEstoqueMinimo(5);

        //Executar
        boolean resultado = produto.estoqueBaixo();

        //Verificar
        assertTrue(resultado);
    }

    @Test
    void deveRetornaFalseQuandoEstoqueForMaiorQueMinimo() {

        //Preparar
        Produto produto = new Produto();
        produto.setQuantidadeEstoque(6);
        produto.setEstoqueMinimo(5);

        //Executar
        boolean resultado = produto.estoqueBaixo();

        //Verificar
        assertFalse(resultado);
    }

    @Test
    void deveRetornaTrueQuandoEstoqueForIgualAoMinimo() {

        //Preparar
        Produto produto = new Produto();
        produto.setQuantidadeEstoque(5);
        produto.setEstoqueMinimo(5);

        //Executar
        boolean resultado = produto.estoqueBaixo();

        //Verificar
        assertTrue(resultado);

    }

    @Test
    void deveCalcularSubTotalDoItem() {

        //Preparar
        ItemVenda itemVenda = new ItemVenda(null, 2, new BigDecimal("49.90"));
        BigDecimal esperado = new BigDecimal("99.80");

        //Executar
        BigDecimal subTotal = itemVenda.getSubTotal();

        //Verificar
        assertEquals(esperado, subTotal);
    }

    @Test
    void deveRetornaPrecoUnitarioQuandoQuantidadeForUm() {

        //Preparar
        ItemVenda itemVenda = new ItemVenda(null, 1, new BigDecimal("49.90"));
        BigDecimal esperado = new BigDecimal("49.90");

        //Executar
        BigDecimal subTotal = itemVenda.getSubTotal();

        //Verificar
        assertEquals(esperado, subTotal);

    }

    @Test
    void deveCalcularValorTotalDaVenda() {

        //Preparar
        Produto p1 = new Produto(
                1L, "Mouse", "PRD-001",
                new BigDecimal("49.90"), 10, 2
        );

        Produto p2 = new Produto(
                2L, "Teclado", "PRD-002",
                new BigDecimal("89.90"), 5, 1
        );

        Venda venda = new Venda();

        ItemVenda itemVenda = new ItemVenda(new ItemVendaPK(venda, p1), 2, new BigDecimal("49.90"));
        ItemVenda itemVenda1 = new ItemVenda(new ItemVendaPK(venda, p2), 1, new BigDecimal("89.90"));

        venda.getItens().add(itemVenda);
        venda.getItens().add(itemVenda1);

        BigDecimal esperado = new BigDecimal("189.70");

        //Executar
        BigDecimal total = venda.getTotal();

        //Verificar
        assertEquals(esperado, total);

    }

    @Test
    void deveRetornaZeroQuandoVendaNaoPossuirItens() {

        //Preparar
        Venda venda = new Venda();
        BigDecimal esperado = BigDecimal.ZERO;

        //Executar
        BigDecimal total = venda.getTotal();

        //Verificar
        assertEquals(esperado, total);
    }


}
