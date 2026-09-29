package com.example.vendasestoque.resources;

import com.example.vendasestoque.entities.Produto;
import com.example.vendasestoque.services.ProdutoService;
import com.example.vendasestoque.services.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProdutoResource.class)
@ActiveProfiles("test")
public class ProdutoResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProdutoService produtoService;

    @Test
    void deveRetornar200paraListarProdutosCadastrados() throws Exception {

        //Preparar
        Produto p1 = new Produto(
                1L, "Mouse", "PRD-001",
                new BigDecimal("49.90"), 10, 2
        );

        Produto p2 = new Produto(
                2L, "Teclado", "PRD-002",
                new BigDecimal("89.90"), 5, 1
        );

        when(produtoService.listarProdutos()).thenReturn(List.of(p1, p2));

        //Executar e Verificar
        mockMvc.perform(get("/produtos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(produtoService).listarProdutos();
    }

    @Test
    void deveRetornar200paraListaVazia() throws Exception {

        //Preparar
        when(produtoService.listarProdutos()).thenReturn(List.of());

        //Executar e Verificar
        mockMvc.perform(get("/produtos"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(produtoService).listarProdutos();
    }

    @Test
    void deveRetornar200paraBuscarProdutoComIdExistente() throws Exception {

        //Preparar
        Produto p1 = new Produto(
                1L, "Mouse", "PRD-001",
                new BigDecimal("49.90"), 10, 2
        );

        when(produtoService.buscarProdutoPorId(1L)).thenReturn(p1);

        //Executar e Verificar
        mockMvc.perform(get("/produtos/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(produtoService).buscarProdutoPorId(1L);

    }

    @Test
    void deveRetornar404paraBuscarProdutoInexistente() throws Exception {

        //Preparar
        when(produtoService.buscarProdutoPorId(99L)).thenThrow(new ResourceNotFoundException(99L));

        //Executar e Verificar
        mockMvc.perform(get("/produtos/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Recurso não encontrado"))
                .andExpect(jsonPath("$.caminho").value("/produtos/99"));

        verify(produtoService).buscarProdutoPorId(99L);

    }

    @Test
    void deveRetornar200paraBuscarProdutoPorCodigoExistente() throws Exception {

        //Preparar
        Produto p1 = new Produto(
                1L, "Mouse", "PRD-001",
                new BigDecimal("49.90"), 10, 2
        );

        when(produtoService.buscarProdutoPorCodigo("PRD-001")).thenReturn(p1);

        //Executar e Verificar
        mockMvc.perform(get("/produtos/codigo?numerobarra=PRD-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(produtoService).buscarProdutoPorCodigo("PRD-001");

    }

    @Test
    void deveRetornar404paraBuscarProdutoComCodigoInexistente() throws Exception {

        //Preparar
        when(produtoService.buscarProdutoPorCodigo("PRD-999")).thenThrow(new ResourceNotFoundException("PRD-999"));

        //Executar e Verificar
        mockMvc.perform(get("/produtos/codigo?numerobarra=PRD-999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Recurso não encontrado"))
                .andExpect(jsonPath("$.caminho").value("/produtos/codigo"));

        verify(produtoService).buscarProdutoPorCodigo("PRD-999");

    }

    @Test
    void deveRetornar200comListagemDosProdutosComBaixaEstoque() throws Exception{

        //Preparar
        Produto p1 = new Produto(
                1L, "Mouse", "PRD-001",
                new BigDecimal("49.90"), 1, 2
        );

        Produto p2 = new Produto(
                2L, "Teclado", "PRD-002",
                new BigDecimal("89.90"), 1, 2
        );

        when(produtoService.buscarProdutosComEstoqueBaixo()).thenReturn(List.of(p1,p2));

        //Executar e Verificar
        mockMvc.perform(get("/produtos/baixoestoque"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(produtoService).buscarProdutosComEstoqueBaixo();

    }

}
