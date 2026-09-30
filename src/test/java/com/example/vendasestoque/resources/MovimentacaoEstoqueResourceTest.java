package com.example.vendasestoque.resources;
import com.example.vendasestoque.dtos.MovimentacaoRequestDTO;
import com.example.vendasestoque.dtos.MovimentacaoUpdateDTO;
import com.example.vendasestoque.entities.Cliente;
import com.example.vendasestoque.entities.MovimentacaoEstoque;
import com.example.vendasestoque.entities.Produto;
import com.example.vendasestoque.entities.Venda;
import com.example.vendasestoque.entities.enuns.StatusVenda;
import com.example.vendasestoque.entities.enuns.TipoMovimentacao;
import com.example.vendasestoque.services.MovimentacaoEstoqueService;
import com.example.vendasestoque.services.exceptions.EstoqueInsuficiente;
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
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MovimentacaoEstoqueResource.class)
@ActiveProfiles("test")
public class MovimentacaoEstoqueResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MovimentacaoEstoqueService movimentacaoEstoqueService;

    @Test
    void deveRetornar200paraMovimentacoesCadastradas() throws Exception {

        //Preparar
        Produto p1 = new Produto(
                null, "Mouse", "PRD-001",
                new BigDecimal("49.90"), 10, 2
        );

        MovimentacaoEstoque entrada = new MovimentacaoEstoque(
                1L,
                TipoMovimentacao.ENTRADA,
                5,
                Instant.now(),
                "Reposição de estoque",
                p1,
                null
        );

        MovimentacaoEstoque saida = new MovimentacaoEstoque(
                2L,
                TipoMovimentacao.SAIDA,
                5,
                Instant.now(),
                "Venda do produto",
                p1,
                null
        );

        when(movimentacaoEstoqueService.listarMovimentacoes()).thenReturn(List.of(entrada, saida));

        //Executar e Verificar
        mockMvc.perform(get("/movimentacoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(movimentacaoEstoqueService).listarMovimentacoes();

    }

    @Test
    void deveRetornar200comListaDeMovimentacoesVazia() throws Exception{

        //Preparar
        when(movimentacaoEstoqueService.listarMovimentacoes()).thenReturn(List.of());

        //Executar e Verificar
        mockMvc.perform(get("/movimentacoes"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(movimentacaoEstoqueService).listarMovimentacoes();

    }

    @Test
    void deveRetornar200aoBuscarMovimentacaoComIdExistente() throws Exception {

        //Preparar
        Produto p1 = new Produto(
                null, "Mouse", "PRD-001",
                new BigDecimal("49.90"), 10, 2
        );

        MovimentacaoEstoque entrada = new MovimentacaoEstoque(
                1L,
                TipoMovimentacao.ENTRADA,
                5,
                Instant.now(),
                "Reposição de estoque",
                p1,
                null
        );

        when(movimentacaoEstoqueService.buscarMovimentacaoPorId(1L)).thenReturn(entrada);

        //Executar e Verificar
        mockMvc.perform(get("/movimentacoes/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(movimentacaoEstoqueService).buscarMovimentacaoPorId(1L);

    }

    @Test
    void deveRetornar404paraBuscaDeMovimentacaoComIdInexistente() throws Exception {

        //Preparar
        when(movimentacaoEstoqueService.buscarMovimentacaoPorId(99L)).thenThrow(new ResourceNotFoundException(99L));

        //Executar e Verificar
        mockMvc.perform(get("/movimentacoes/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Recurso não encontrado"))
                .andExpect(jsonPath("$.caminho").value("/movimentacoes/99"));

        verify(movimentacaoEstoqueService).buscarMovimentacaoPorId(99L);

    }

    @Test
    void deveRetornar200paraBuscaDeMovimentacaoSemVendaVinculada() throws Exception {

        //Preparar
        Produto p1 = new Produto(
                null, "Mouse", "PRD-001",
                new BigDecimal("49.90"), 10, 2
        );

        MovimentacaoEstoque entrada = new MovimentacaoEstoque(
                1L,
                TipoMovimentacao.SAIDA,
                5,
                Instant.now(),
                "Reposição de estoque",
                p1,
                null
        );

       when(movimentacaoEstoqueService.buscarMovimentacaoPorId(1L)).thenReturn(entrada);

       //Executar e Verificar
        mockMvc.perform(get("/movimentacoes/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(movimentacaoEstoqueService).buscarMovimentacaoPorId(1L);

    }

    @Test
    void deveRetornar200paraBuscaDeMovimentacaoComVendaVinculada() throws Exception {

        //Preparar
        Cliente c1 = new Cliente(
                1L, "Maria Silva",
                "maria@example.com", "11987654321"
        );

        Produto p1 = new Produto(
                1L, "Mouse", "PRD-001",
                new BigDecimal("49.90"), 10, 2
        );

        Venda v1 = new Venda(
                1L,
                Instant.parse("2026-09-21T13:00:00Z"),
                StatusVenda.CONFIRMADA,
                c1
        );

        MovimentacaoEstoque saida = new MovimentacaoEstoque(
                1L,
                TipoMovimentacao.SAIDA,
                5,
                Instant.now(),
                "Venda do produto",
                p1,
                v1
        );

        when(movimentacaoEstoqueService.buscarMovimentacaoPorId(1L)).thenReturn(saida);

        //Executar e Verificar
        mockMvc.perform(get("/movimentacoes/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(movimentacaoEstoqueService).buscarMovimentacaoPorId(1L);

    }

    @Test
    void deveRetornar201aoCadastrarMovimentacaoSaidaValida() throws Exception{

        //Preparar
        Cliente c1 = new Cliente(
                1L, "Maria Silva",
                "maria@example.com", "11987654321"
        );

        Produto p1 = new Produto(
                1L, "Mouse", "PRD-001",
                new BigDecimal("49.90"), 10, 2
        );

        Venda v1 = new Venda(
                1L,
                Instant.parse("2026-09-21T13:00:00Z"),
                StatusVenda.CONFIRMADA,
                c1
        );

        MovimentacaoEstoque saida = new MovimentacaoEstoque(
                1L,
                TipoMovimentacao.SAIDA,
                5,
                Instant.now(),
                "Venda do produto",
                p1,
                v1
        );

        when(movimentacaoEstoqueService.cadastrarMovimentacao(
                any(MovimentacaoRequestDTO.class)
        )).thenReturn(saida);

        String json = """
                {
                "produtoId": 1,
                "tipoMovimentacao": "SAIDA",
                "quantidade": 5,
                "motivo": "Venda do produto"
                }
                """;

        //Executar e Verificar
        mockMvc.perform(post("/movimentacoes").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/movimentacoes/1"))
                .andExpect(jsonPath("$.id").value(1));


        ArgumentCaptor<MovimentacaoRequestDTO> captor =
                ArgumentCaptor.forClass(MovimentacaoRequestDTO.class);

        verify(movimentacaoEstoqueService)
                .cadastrarMovimentacao(captor.capture());

        MovimentacaoRequestDTO dto = captor.getValue();

        assertEquals(TipoMovimentacao.SAIDA, dto.tipoMovimentacao());
        assertEquals(p1.getId(), dto.produtoId());

    }

    @Test
    void deveRetornar201aoCadastrarMovimentacaoEntradaValida() throws Exception {

        //Preparar
        Produto p1 = new Produto(
                1L, "Mouse", "PRD-001",
                new BigDecimal("49.90"), 10, 2
        );

        MovimentacaoEstoque entrada = new MovimentacaoEstoque(
                1L,
                TipoMovimentacao.ENTRADA,
                5,
                Instant.now(),
                "Reposição Estoque",
                p1,
                null
        );

        when(movimentacaoEstoqueService.cadastrarMovimentacao(any(MovimentacaoRequestDTO.class))).thenReturn(entrada);

        String json = """
                {
                "produtoId": 1,
                "tipoMovimentacao": "ENTRADA",
                "quantidade": 5,
                "motivo": "Venda do produto"
                }
                """;

        //Executar e Verificar
        mockMvc.perform(post("/movimentacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/movimentacoes/1"))
                .andExpect(jsonPath("$.id").value(1));

        ArgumentCaptor<MovimentacaoRequestDTO> captor = ArgumentCaptor.forClass(MovimentacaoRequestDTO.class);
        verify(movimentacaoEstoqueService).cadastrarMovimentacao(captor.capture());
        MovimentacaoRequestDTO dto = captor.getValue();

        assertEquals(TipoMovimentacao.ENTRADA, dto.tipoMovimentacao());
        assertEquals(p1.getId(), dto.produtoId());

    }

    @Test
    void deveRetornar404paraMovimentacaoComProdutoInexistente() throws Exception {

        //Preparar
       when(movimentacaoEstoqueService.cadastrarMovimentacao(any(MovimentacaoRequestDTO.class)))
               .thenThrow(new ResourceNotFoundException(99L));

        String json = """
                {
                "produtoId": 99,
                "tipoMovimentacao": "ENTRADA",
                "quantidade": 5,
                "motivo": "Venda do produto"
                }
                """;

       //Executar e Verificar
        mockMvc.perform(post("/movimentacoes")
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Recurso não encontrado"))
                .andExpect(jsonPath("$.caminho").value("/movimentacoes"));

        verify(movimentacaoEstoqueService).cadastrarMovimentacao(any(MovimentacaoRequestDTO.class));

    }

    @Test
    void deveRetornar409paraMovimentacaoDeSaidaComEstoqueInsuficiente() throws Exception {

       //Preparar
        when(movimentacaoEstoqueService.cadastrarMovimentacao(any(MovimentacaoRequestDTO.class)))
                .thenThrow(new EstoqueInsuficiente("Estoque insuficiente"));

        String json = """
                {
                "produtoId": 1,
                "tipoMovimentacao": "SAIDA",
                "quantidade": 12,
                "motivo": "Venda do produto"
                }
                """;

        //Executar e Verificar
        mockMvc.perform(post("/movimentacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Estoque insuficiente"))
                .andExpect(jsonPath("$.caminho").value("/movimentacoes"));

        verify(movimentacaoEstoqueService).cadastrarMovimentacao(any(MovimentacaoRequestDTO.class));

    }

    @Test
    void deveRetornar400aoCadastrarMovimentacaoComProdutoNulo() throws Exception{

        //Preparar
        String json = """
                {
                "produtoId": null,
                "tipoMovimentacao": "SAIDA",
                "quantidade": 5,
                "motivo": "Venda do produto"
                }
                """;

        //Executar e Verificar
        mockMvc.perform(post("/movimentacoes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());

        verify(movimentacaoEstoqueService, never()).cadastrarMovimentacao(any(MovimentacaoRequestDTO.class));

    }

    @Test
    void deveRetornar400aoCadastrarMovimentacaoComProdutoId0() throws Exception{

        //Preparar
        String json = """
                {
                "produtoId": 0,
                "tipoMovimentacao": "SAIDA",
                "quantidade": 5,
                "motivo": "Venda do produto"
                }
                """;

        //Executar e Verificar
        mockMvc.perform(post("/movimentacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(movimentacaoEstoqueService, never()).cadastrarMovimentacao(any(MovimentacaoRequestDTO.class));

    }

    @Test
    void deveRetornar400aoCadastrarMovimentacaoComProdutoIdNegativo() throws Exception{

        //Preparar
        String json = """
                {
                "produtoId": -1,
                "tipoMovimentacao": "SAIDA",
                "quantidade": 5,
                "motivo": "Venda do produto"
                }
                """;

        //Executar e Verificar
        mockMvc.perform(post("/movimentacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(movimentacaoEstoqueService, never()).cadastrarMovimentacao(any(MovimentacaoRequestDTO.class));

    }

    @Test
    void deveRetornar400aoCadastrarMovimentacaoComQuantidadeNula() throws Exception{

        //Preparar
        String json = """
                {
                "produtoId": 1,
                "tipoMovimentacao": "SAIDA",
                "quantidade": null,
                "motivo": "Venda do produto"
                }
                """;

        //Executar e Verificar
        mockMvc.perform(post("/movimentacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(movimentacaoEstoqueService, never()).cadastrarMovimentacao(any(MovimentacaoRequestDTO.class));

    }

    @Test
    void deveRetornar400aoCadastrarMovimentacaoComQuantidadeIgualA0() throws Exception{

        //Preparar
        String json = """
                {
                "produtoId": 1,
                "tipoMovimentacao": "SAIDA",
                "quantidade": 0,
                "motivo": "Venda do produto"
                }
                """;

        //Executar e Verificar
        mockMvc.perform(post("/movimentacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(movimentacaoEstoqueService, never()).cadastrarMovimentacao(any(MovimentacaoRequestDTO.class));

    }

    @Test
    void deveRetornar400aoCadastrarMovimentacaoComQuantidadeNegativa() throws Exception{

        //Preparar
        String json = """
                {
                "produtoId": 1,
                "tipoMovimentacao": "SAIDA",
                "quantidade": -1,
                "motivo": "Venda do produto"
                }
                """;

        //Executar e Verificar
        mockMvc.perform(post("/movimentacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(movimentacaoEstoqueService, never()).cadastrarMovimentacao(any(MovimentacaoRequestDTO.class));

    }

    @Test
    void deveRetornar400aoCadastrarMovimentacaoComTextoInvalidaNoTipoDeMovimentacao() throws Exception {

        //Preparar
        String json = """
                {
                "produtoId": 1,
                "tipoMovimentacao": "INVALIDO",
                "quantidade": 5,
                "motivo": "Venda do produto"
                }
                """;

        //Executar e Verificar
        mockMvc.perform(post("/movimentacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(movimentacaoEstoqueService, never()).cadastrarMovimentacao(any(MovimentacaoRequestDTO.class));

    }

    @Test
    void deveRetornar400aoCadastrarMovimentacaoComMotivoNulo() throws Exception{

        //Preparar
        String json = """
                {
                "produtoId": 1,
                "tipoMovimentacao": "SAIDA",
                "quantidade": 5,
                "motivo": null
                }
                """;

        //Executar e Verificar
        mockMvc.perform(post("/movimentacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(movimentacaoEstoqueService, never()).cadastrarMovimentacao(any(MovimentacaoRequestDTO.class));

    }

    @Test
    void deveRetornar400aoCadastrarMovimentacaoComMotivoVazio() throws Exception{

        //Preparar
        String json = """
                {
                "produtoId": 1,
                "tipoMovimentacao": "SAIDA",
                "quantidade": 5,
                "motivo": ""
                }
                """;

        //Executar e Verificar
        mockMvc.perform(post("/movimentacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(movimentacaoEstoqueService, never()).cadastrarMovimentacao(any(MovimentacaoRequestDTO.class));

    }

    @Test
    void deveRetornar400aoCadastrarMovimentacaoComMotivoSomenteComEspacos() throws Exception{

        //Preparar
        String json = """
                {
                "produtoId": 1,
                "tipoMovimentacao": "SAIDA",
                "quantidade": 5,
                "motivo": "  "
                }
                """;

        //Executar e Verificar
        mockMvc.perform(post("/movimentacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(movimentacaoEstoqueService, never()).cadastrarMovimentacao(any(MovimentacaoRequestDTO.class));

    }

    @Test
    void deveRetornar200aoAtualizarMotivoDeMovimentacaoExistente() throws Exception {

        //Preparar
        Produto p1 = new Produto(
                1L, "Mouse", "PRD-001",
                new BigDecimal("49.90"), 10, 2
        );

        MovimentacaoEstoque entradaAtualizada = new MovimentacaoEstoque(
                1L,
                TipoMovimentacao.ENTRADA,
                5,
                Instant.now(),
                "Repondo produto",
                p1,
                null
        );

        when(movimentacaoEstoqueService.atualizarMovimentacao(eq(1L), any(MovimentacaoUpdateDTO.class)))
                .thenReturn(entradaAtualizada);

        String json = """
                {
                "motivo": "Repondo produto"
                }
                """;

        //Executar e Verificar
        mockMvc.perform(patch("/movimentacoes/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        ArgumentCaptor<MovimentacaoUpdateDTO> captor = ArgumentCaptor.forClass(MovimentacaoUpdateDTO.class);
        verify(movimentacaoEstoqueService).atualizarMovimentacao(eq(1L), captor.capture());
        MovimentacaoUpdateDTO dto = captor.getValue();

        assertEquals("Repondo produto", dto.motivo());

    }

    @Test
    void deveRetornar404aoAtualizarMotivoDeMovimentacaoInexistente() throws Exception {

        //Preparar
        String json = """
                {
                "motivo": "Repondo produto"
                }
                """;

        when(movimentacaoEstoqueService.atualizarMovimentacao(eq(99L), any(MovimentacaoUpdateDTO.class)))
                .thenThrow(new ResourceNotFoundException(99L));

        //Executar e Verificar
        mockMvc.perform(patch("/movimentacoes/{id}", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Recurso não encontrado"))
                .andExpect(jsonPath("$.caminho").value("/movimentacoes/99"));

        verify(movimentacaoEstoqueService).atualizarMovimentacao(eq(99L), any(MovimentacaoUpdateDTO.class));

    }

    @Test
    void deveRetornar400aoAtualizarComMotivoNulo() throws Exception {

        //Preparar
        String json = """
                {
                "motivo" : null
                }
                """;

        //Executar e Verificar
        mockMvc.perform(patch("/movimentacoes/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());

        verify(movimentacaoEstoqueService, never()).atualizarMovimentacao(eq(1L), any(MovimentacaoUpdateDTO.class));

    }

    @Test
    void deveRetornar400aoAtualizarComMotivoVazio() throws Exception {

        //Preparar
        String json = """
               {
                "motivo" : ""
               }
                """;

        //Executar e Verificar
        mockMvc.perform(patch("/movimentacoes/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(movimentacaoEstoqueService, never()).atualizarMovimentacao(eq(1L), any(MovimentacaoUpdateDTO.class));

    }

    @Test
    void deveRetornar400aoAtualizarComMotivoComApenasEspacos() throws Exception {

        //Preparar
        String json = """
                {
                "motivo" : "  "
                }
                """;

        //Executar e Verificar
        mockMvc.perform(patch("/movimentacoes/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(movimentacaoEstoqueService, never()).atualizarMovimentacao(eq(1L), any(MovimentacaoUpdateDTO.class));

    }

}
