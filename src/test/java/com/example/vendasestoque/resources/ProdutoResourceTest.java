package com.example.vendasestoque.resources;

import com.example.vendasestoque.dtos.ProdutoRequestDTO;
import com.example.vendasestoque.entities.Produto;
import com.example.vendasestoque.services.ProdutoService;
import com.example.vendasestoque.services.exceptions.CodigoExistente;
import com.example.vendasestoque.services.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.security.ProtectionDomain;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
        Produto p1 = new Produto(1L, "Mouse", "PRD-001", new BigDecimal("49.90"), 10, 2);

        Produto p2 = new Produto(2L, "Teclado", "PRD-002", new BigDecimal("89.90"), 5, 1);

        when(produtoService.listarProdutos()).thenReturn(List.of(p1, p2));

        //Executar e Verificar
        mockMvc.perform(get("/produtos")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2)).andExpect(jsonPath("$[0].id").value(1)).andExpect(jsonPath("$[1].id").value(2));

        verify(produtoService).listarProdutos();
    }

    @Test
    void deveRetornar200paraListaVazia() throws Exception {

        //Preparar
        when(produtoService.listarProdutos()).thenReturn(List.of());

        //Executar e Verificar
        mockMvc.perform(get("/produtos")).andExpect(status().isOk()).andExpect(content().json("[]"));

        verify(produtoService).listarProdutos();
    }

    @Test
    void deveRetornar200paraBuscarProdutoComIdExistente() throws Exception {

        //Preparar
        Produto p1 = new Produto(1L, "Mouse", "PRD-001", new BigDecimal("49.90"), 10, 2);

        when(produtoService.buscarProdutoPorId(1L)).thenReturn(p1);

        //Executar e Verificar
        mockMvc.perform(get("/produtos/{id}", 1)).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1));

        verify(produtoService).buscarProdutoPorId(1L);

    }

    @Test
    void deveRetornar404paraBuscarProdutoInexistente() throws Exception {

        //Preparar
        when(produtoService.buscarProdutoPorId(99L)).thenThrow(new ResourceNotFoundException(99L));

        //Executar e Verificar
        mockMvc.perform(get("/produtos/{id}", 99L)).andExpect(status().isNotFound()).andExpect(jsonPath("$.error").value("Recurso não encontrado")).andExpect(jsonPath("$.caminho").value("/produtos/99"));

        verify(produtoService).buscarProdutoPorId(99L);

    }

    @Test
    void deveRetornar200paraBuscarProdutoPorCodigoExistente() throws Exception {

        //Preparar
        Produto p1 = new Produto(1L, "Mouse", "PRD-001", new BigDecimal("49.90"), 10, 2);

        when(produtoService.buscarProdutoPorCodigo("PRD-001")).thenReturn(p1);

        //Executar e Verificar
        mockMvc.perform(get("/produtos/codigo?numerobarra=PRD-001")).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1));

        verify(produtoService).buscarProdutoPorCodigo("PRD-001");

    }

    @Test
    void deveRetornar404paraBuscarProdutoComCodigoInexistente() throws Exception {

        //Preparar
        when(produtoService.buscarProdutoPorCodigo("PRD-999")).thenThrow(new ResourceNotFoundException("PRD-999"));

        //Executar e Verificar
        mockMvc.perform(get("/produtos/codigo?numerobarra=PRD-999")).andExpect(status().isNotFound()).andExpect(jsonPath("$.error").value("Recurso não encontrado")).andExpect(jsonPath("$.caminho").value("/produtos/codigo"));

        verify(produtoService).buscarProdutoPorCodigo("PRD-999");

    }

    @Test
    void deveRetornar200comListagemDosProdutosComBaixaEstoque() throws Exception {

        //Preparar
        Produto p1 = new Produto(1L, "Mouse", "PRD-001", new BigDecimal("49.90"), 1, 2);

        Produto p2 = new Produto(2L, "Teclado", "PRD-002", new BigDecimal("89.90"), 1, 2);

        when(produtoService.buscarProdutosComEstoqueBaixo()).thenReturn(List.of(p1, p2));

        //Executar e Verificar
        mockMvc.perform(get("/produtos/baixoestoque")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2)).andExpect(jsonPath("$[0].id").value(1)).andExpect(jsonPath("$[1].id").value(2));

        verify(produtoService).buscarProdutosComEstoqueBaixo();

    }

    @Test
    void deveRetornar200paraListagemVaziaDeProdutosComBaixoEstoque() throws Exception {

        //Preparar
        when(produtoService.buscarProdutosComEstoqueBaixo()).thenReturn(List.of());

        //Executar e Verificar
        mockMvc.perform(get("/produtos/baixoestoque")).andExpect(status().isOk()).andExpect(content().json("[]"));

        verify(produtoService).buscarProdutosComEstoqueBaixo();
    }

    @Test
    void deveRetornar201aoCadastrarProdutoValido() throws Exception {

        //Preparar
        Produto p1 = new Produto(1L, "Teclado", "PRD-003", new BigDecimal("199.90"), 0, 5);

        when(produtoService.cadastrarProduto(any(ProdutoRequestDTO.class))).thenReturn(p1);

        String json = """
                {
                "nome" : "Teclado",
                "codigo" : "PRD-003",
                "preco" : 199.90,
                "estoqueMinimo" : 5
                }
                """;

        //Executar e Verificar
        mockMvc.perform(post("/produtos").contentType(MediaType.APPLICATION_JSON).content(json)).andExpect(status().isCreated()).andExpect(header().string("Location", "http://localhost/produtos/1")).andExpect(jsonPath("$.id").value(1));

        ArgumentCaptor<ProdutoRequestDTO> captor = ArgumentCaptor.forClass(ProdutoRequestDTO.class);
        verify(produtoService).cadastrarProduto(captor.capture());
        ProdutoRequestDTO dto = captor.getValue();

        assertEquals(p1.getNome(), dto.nome());
        assertEquals(p1.getCodigo(), dto.codigo());
        assertEquals(p1.getPreco(), dto.preco());
        assertEquals(p1.getEstoqueMinimo(), dto.estoqueMinimo());
        verify(produtoService).cadastrarProduto(dto);

    }

    @Test
    void deveRetornar409paraCadastroDeProdutoComCodigoExistente() throws Exception {

        //Preparar
        String json = """
                {
                "nome" : "Teclado",
                "codigo" : "PRD-003",
                "preco" : 199.90,
                "estoqueMinimo" : 5
                }
                """;

        when(produtoService.cadastrarProduto(any(ProdutoRequestDTO.class))).thenThrow(new CodigoExistente("Código já existe"));

        //Executar e Verificar
        mockMvc.perform(post("/produtos").contentType(MediaType.APPLICATION_JSON).content(json)).andExpect(status().isConflict()).andExpect(jsonPath("$.error").value("Código inválido")).andExpect(jsonPath("$.caminho").value("/produtos"));

        verify(produtoService).cadastrarProduto(any(ProdutoRequestDTO.class));

    }

    @Test
    void deveRetornar400aoCadastrarComNomeNulo() throws Exception {

        //Preparar
        String json = """
                {
                "nome" : null,
                "codigo" : "PRD-003",
                "preco" : 199.90,
                "estoqueMinimo" : 5
                }
                """;

        //Executar e Verificar
        mockMvc.perform(post("/produtos").contentType(MediaType.APPLICATION_JSON).content(json)).andExpect(status().isBadRequest());

        verify(produtoService, never()).cadastrarProduto(any(ProdutoRequestDTO.class));

    }

    @Test
    void deveRetornar400aoCadastrarComNomeVazio() throws Exception {

        //Preparar
        String json = """
                {
                "nome" : "",
                "codigo" : "PRD-003",
                "preco" : 199.90,
                "estoqueMinimo" : 5
                }
                """;

        //Executar e Verificar
        mockMvc.perform(post("/produtos").contentType(MediaType.APPLICATION_JSON).content(json)).andExpect(status().isBadRequest());

        verify(produtoService, never()).cadastrarProduto(any(ProdutoRequestDTO.class));

    }

    @Test
    void deveRetornar400aoCadastrarComNomeSomenteComEspacos() throws Exception {

        //Preparar
        String json = """
                {
                "nome" : "  ",
                "codigo" : "PRD-003",
                "preco" : 199.90,
                "estoqueMinimo" : 5
                }
                """;

        //Executar e Verificar
        mockMvc.perform(post("/produtos").contentType(MediaType.APPLICATION_JSON).content(json)).andExpect(status().isBadRequest());

        verify(produtoService, never()).cadastrarProduto(any(ProdutoRequestDTO.class));

    }

    @Test
    void deveRetornar400aoCadastrarProdutoComCodigoNulo() throws Exception {

        //Preparar
        String json = """
                {
                "nome" : "Teclado",
                "codigo" : null,
                "preco" : 199.90,
                "estoqueMinimo" : 5
                }
                """;

        //Executar e Verificar
        mockMvc.perform(post("/produtos").contentType(MediaType.APPLICATION_JSON).content(json)).andExpect(status().isBadRequest());

    }

    @Test
    void deveRetornar400aoCadastrarProdutoComCodigoVazio() throws Exception {

        //Preparar
        String json = """
                {
                "nome" : "Teclado",
                "codigo" : "",
                "preco" : 199.90,
                "estoqueMinimo" : 5
                }
                """;

        //Executar e Verificar
        mockMvc.perform(post("/produtos").contentType(MediaType.APPLICATION_JSON).content(json)).andExpect(status().isBadRequest());

    }

    @Test
    void deveRetornar400aoCadastrarProdutoComCodigoSomenteComEspacos() throws Exception {

        //Preparar
        String json = """
                {
                "nome" : "Teclado",
                "codigo" : "  ",
                "preco" : 199.90,
                "estoqueMinimo" : 5
                }
                """;

        //Executar e Verificar
        mockMvc.perform(post("/produtos").contentType(MediaType.APPLICATION_JSON).content(json)).andExpect(status().isBadRequest());

    }

    @Test
    void deveRetornar400aoCadastrarProdutoComPrecoNulo() throws Exception {

        //Preparar
        String json = """
                {
                "nome" : "Teclado",
                "codigo" : "PRD-003",
                "preco" : null,
                "estoqueMinimo" : 5
                }
                """;

        //Executar e Verificar
        mockMvc.perform(post("/produtos").contentType(MediaType.APPLICATION_JSON).content(json)).andExpect(status().isBadRequest());

    }

    @Test
    void deveRetornar400aoCadastrarProdutoComPrecoVazio() throws Exception {

        //Preparar
        String json = """
                {
                "nome" : "Teclado",
                "codigo" : "PRD-003",
                "preco" : "",
                "estoqueMinimo" : 5
                }
                """;

        //Executar e Verificar
        mockMvc.perform(post("/produtos").contentType(MediaType.APPLICATION_JSON).content(json)).andExpect(status().isBadRequest());

    }

    @Test
    void deveRetornar400aoCadastrarProdutoComPrecoSomenteComEspacos() throws Exception {

        //Preparar
        String json = """
                {
                "nome" : "Teclado",
                "codigo" : "PRD-003",
                "preco" : "  ",
                "estoqueMinimo" : 5
                }
                """;

        //Executar e Verificar
        mockMvc.perform(post("/produtos").contentType(MediaType.APPLICATION_JSON).content(json)).andExpect(status().isBadRequest());

    }

    @Test
    void deveRetornar400aoCadastrarProdutoComEstoqueMinimoNulo() throws Exception {

        //Preparar
        String json = """
                {
                "nome" : "Teclado",
                "codigo" : "PRD-003",
                "preco" : 199.90,
                "estoqueMinimo" : null
                }
                """;

        //Executar e Verificar
        mockMvc.perform(post("/produtos").contentType(MediaType.APPLICATION_JSON).content(json)).andExpect(status().isBadRequest());

    }

    @Test
    void deveRetornar400aoCadastrarProdutoComEstoqueMinimoNegativo() throws Exception {

        //Preparar
        String json = """
                {
                "nome" : "Teclado",
                "codigo" : "PRD-003",
                "preco" : 199.90,
                "estoqueMinimo" : -1
                }
                """;

        //Executar e Verificar
        mockMvc.perform(post("/produtos").contentType(MediaType.APPLICATION_JSON).content(json)).andExpect(status().isBadRequest());

    }

    @Test
    void deveRetornar200aoAtualizarProdutoComDadosValidos() throws Exception {

        //Preparar
        Produto p1 = new Produto(1L, "Teclado", "PRD-003", new BigDecimal("199.90"), 0, 5);

        when(produtoService.atualizarProduto(eq(1L), any(ProdutoRequestDTO.class))).thenReturn(p1);

        String json = """
                {
                    "nome" : "Teclado",
                    "codigo" : "PRD-003",
                    "preco" : 199.90,
                    "estoqueMinimo" : 5
                }
                """;

        //Executar e Verificar
        mockMvc.perform(put("/produtos/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        ArgumentCaptor<ProdutoRequestDTO> captor = ArgumentCaptor.forClass(ProdutoRequestDTO.class);
        verify(produtoService).atualizarProduto(eq(1L), captor.capture());
        ProdutoRequestDTO dto = captor.getValue();

        assertEquals(p1.getNome(), dto.nome());
        assertEquals(p1.getCodigo(), dto.codigo());
        assertEquals(p1.getPreco(), dto.preco());
        assertEquals(p1.getEstoqueMinimo(), dto.estoqueMinimo());
        verify(produtoService).atualizarProduto(1L, dto);

    }

    @Test
    void deveRetornar404aoAtualizarProdutoInexistente() throws Exception {

        //Preparar
        String json = """
                {
                    "nome" : "Teclado",
                    "codigo" : "PRD-003",
                    "preco" : 199.90,
                    "estoqueMinimo" : 5
                }
                """;

        when(produtoService.atualizarProduto(eq(99L), any(ProdutoRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException(99L));

        //Executar e Verificar
        mockMvc.perform(put("/produtos/{id}", 99L)
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Recurso não encontrado"))
                .andExpect(jsonPath("$.caminho").value("/produtos/99"));

        verify(produtoService).atualizarProduto(eq(99L), any(ProdutoRequestDTO.class));

    }

    @Test
    void deveRetornar409aoTentarAtualizarCodigoParaUmCodigoJaCadastradoEmOutroProduto() throws Exception {

        //Preparar
        String json = """
                {
                    "nome" : "Teclado",
                    "codigo" : "PRD-003",
                    "preco" : 199.90,
                    "estoqueMinimo" : 5
                }
                """;

        when(produtoService.atualizarProduto(eq(1L), any(ProdutoRequestDTO.class)))
                .thenThrow(new CodigoExistente("Código inválido"));

        //Executar e Verificar
        mockMvc.perform(put("/produtos/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Código inválido"));

        verify(produtoService).atualizarProduto(eq(1L), any(ProdutoRequestDTO.class));

    }

    @Test
    void deveRetornar400aoTentarAtualizarComCamposInvalidos() throws Exception {

        //Preparar
        String json = """
                {
                    "nome" : "Teclado",
                    "codigo" : "PRD-003",
                    "preco" : null,
                    "estoqueMinimo" : 5
                }
                """;

        //Executar e Verificar
        mockMvc.perform(put("/produtos/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());

        verify(produtoService, never()).atualizarProduto(eq(1L), any(ProdutoRequestDTO.class));

    }

    @Test
    void deveRetornar204aoExcluirProdutoExistente() throws Exception {

        //Executar e Verificar
        mockMvc.perform(delete("/produtos/{id}", 10L))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(produtoService).deletarProduto(10L);

    }

    @Test
    void deveRetornar404aoExcluirProdutoInexistente() throws Exception {

        //Preparar
        doThrow(new ResourceNotFoundException(99L)).when(produtoService).deletarProduto(99L);

        //Executar e Verificar
        mockMvc.perform(delete("/produtos/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Recurso não encontrado"))
                .andExpect(jsonPath("$.caminho").value("/produtos/99"));

        verify(produtoService).deletarProduto(99L);

    }

}
