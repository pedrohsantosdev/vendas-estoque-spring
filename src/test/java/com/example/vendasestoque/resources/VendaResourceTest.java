package com.example.vendasestoque.resources;

import com.example.vendasestoque.dtos.VendaRequestDTO;
import com.example.vendasestoque.entities.Cliente;
import com.example.vendasestoque.entities.ItemVenda;
import com.example.vendasestoque.entities.PK.ItemVendaPK;
import com.example.vendasestoque.entities.Produto;
import com.example.vendasestoque.entities.Venda;
import com.example.vendasestoque.entities.enuns.StatusVenda;
import com.example.vendasestoque.services.VendaService;
import com.example.vendasestoque.services.exceptions.ResourceNotFoundException;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
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

@WebMvcTest(VendaResource.class)
@ActiveProfiles("test")
public class VendaResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VendaService vendaService;

    @Test
    void deveRetornar400quandoClienteIdNaoForInformado() throws Exception {

        //Preparar
        String json = """
                {
                                 "itens": [
                                     {
                                         "produtoId": 1,
                                         "quantidade": 2
                                     }
                                 ]
                    }
                """;

        //Executar e verificar
        mockMvc.perform(post("/vendas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(vendaService, never()).cadastrarVenda(any(VendaRequestDTO.class));
    }

    @Test
    void deveRetornar400paraQuantidadeDeItens0() throws Exception {

        //Preparar
        String json = """
                {
                                 "clienteId: 1,   
                                 "itens": [
                                     {
                                         "produtoId": 1,
                                         "quantidade": 0
                                     }
                                 ]
                    }
                """;

        //Executar e verificar
        mockMvc.perform(post("/vendas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(vendaService, never()).cadastrarVenda(any(VendaRequestDTO.class));
    }

    @Test
    void deveCadastrarVendaRetornando201() throws Exception {

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
                10L,
                Instant.now(),
                StatusVenda.CONFIRMADA,
                c1
        );

        ItemVenda itemVenda = new ItemVenda(new ItemVendaPK(v1, p1), 2, p1.getPreco());

        v1.getItens().add(itemVenda);

        when(vendaService.cadastrarVenda(any(VendaRequestDTO.class))).thenReturn(v1);

        String json = """
                {
                                 "clienteId": 1,   
                                 "itens": [
                                     {
                                         "produtoId": 1,
                                         "quantidade": 2
                                     }
                                 ]
                    }
                """;

        //Executar e verificar
        mockMvc.perform(post("/vendas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location", "http://localhost/vendas/10"))
                .andExpect(jsonPath("$.id").value(10));

        ArgumentCaptor<VendaRequestDTO> captor = ArgumentCaptor.forClass(VendaRequestDTO.class);
        verify(vendaService).cadastrarVenda(captor.capture());
        VendaRequestDTO requestDTO = captor.getValue();

        assertEquals(1L, requestDTO.clienteId());
        assertEquals(1, requestDTO.itens().size());
        assertEquals(1L, requestDTO.itens().get(0).produtoId());
        assertEquals(2, requestDTO.itens().get(0).quantidade());
    }

    @Test
    void deveRetornar404QuandoClienteNaoExistir() throws Exception {

        //Preparar
        String json = """
                {
                                 "clienteId": 99,
                                 "itens": [
                                     {
                                         "produtoId": 1,
                                         "quantidade": 2
                                     }
                                 ]
                    }
                """;

        ResourceNotFoundException resourceNotFoundException = new ResourceNotFoundException(99L);

        when(vendaService.cadastrarVenda(any(VendaRequestDTO.class))).thenThrow(resourceNotFoundException);

        //Executar e verificar
        mockMvc.perform(post("/vendas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());

        verify(vendaService).cadastrarVenda(any(VendaRequestDTO.class));
    }

    @Test
    void deveCancelarVendaRetornando200() throws Exception {

        //Preparar
        Cliente c1 = new Cliente(
                1L, "Maria Silva",
                "maria@example.com", "11987654321"
        );

        Venda v1 = new Venda(
                10L,
                Instant.now(),
                StatusVenda.CANCELADA,
                c1
        );

        when(vendaService.cancelarVenda(10L)).thenReturn(v1);

        // Executar e verificar
        mockMvc.perform(patch("/vendas/{id}", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.statusVenda").value("CANCELADA"));

        verify(vendaService).cancelarVenda(10L);
    }

    @Test
    void deveRetornar404aoCancelarVendaInexistente() throws Exception {

        //Preparar
        when(vendaService.cancelarVenda(99L)).thenThrow(new ResourceNotFoundException(99L));

        //Executar e Verificar
        mockMvc.perform(patch("/vendas/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Recurso não encontrado"))
                .andExpect(jsonPath("$.caminho").value("/vendas/99"));

        verify(vendaService).cancelarVenda(99L);
    }

    @Test
    void deveRetornar400QuandoAlistaDeItensForVazia() throws Exception {

        //Preparar
        String json = """
                {
                  "clienteId": 1,
                  "itens": []
                }
                """;

        //Executar e Verificar
        mockMvc.perform(post("/vendas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(vendaService, never()).cadastrarVenda(any(VendaRequestDTO.class));

    }

    @Test
    void deveRetornar200paraUmVendaComIdExistente() throws Exception {

        //Preparar
        Cliente c1 = new Cliente(
                10L, "Maria Silva",
                "maria@example.com", "11987654321"
        );

        Venda v1 = new Venda(
                10L,
                Instant.now(),
                StatusVenda.CONFIRMADA,
                c1
        );

        when(vendaService.buscarVendaPorId(10L)).thenReturn(v1);

        //Executar e Verificar
        mockMvc.perform(get("/vendas/{id}", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.statusVenda").value("CONFIRMADA"));

        verify(vendaService).buscarVendaPorId(10L);

    }

    @Test
    void deveRetorna404quandoBuscarUmaVendaInexistente() throws Exception {

        //Preparar
        when(vendaService.buscarVendaPorId(99L)).thenThrow(new ResourceNotFoundException(99L));

        //Executar e Verificar
        mockMvc.perform(get("/vendas/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Recurso não encontrado"))
                .andExpect(jsonPath("$.caminho").value("/vendas/99"));

        verify(vendaService).buscarVendaPorId(99L);
    }

    @Test
    void deveRetornar200comUmaListaVaziaQuandoNaoExistirVendasCadastradas() throws Exception {

        //Preparar
        when(vendaService.listarVendas()).thenReturn(List.of());

        //Executar e Verificar
        mockMvc.perform(get("/vendas"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(vendaService).listarVendas();
    }

    @Test
    void deveRetornar200comUmaListaDeVendas() throws Exception {

        //Preparar
        Cliente c1 = new Cliente(
                null, "Maria Silva",
                "maria@example.com", "11987654321"
        );

        Venda v1 = new Venda(
                1L,
                Instant.parse("2026-09-21T13:00:00Z"),
                StatusVenda.CONFIRMADA,
                c1
        );

        Venda v2 = new Venda(
                2L,
                Instant.parse("2026-09-21T14:00:00Z"),
                StatusVenda.CANCELADA,
                c1
        );

        when(vendaService.listarVendas()).thenReturn(List.of(v1, v2));

        //Executar e Verificar
        mockMvc.perform(get("/vendas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].statusVenda").value("CONFIRMADA"));
    }

    @Test
    void deveRetornar400QuandoIdDaVendaNaoForNumerico() throws Exception {

        //Executar e Verificar
        mockMvc.perform(get("/vendas/abc"))
                .andExpect(status().isBadRequest());

        verify(vendaService, never()).buscarVendaPorId(anyLong());
    }

    @Test
    void deveRetornar400QuandoListaForNula() throws Exception {

        //Preparar
        String json = """
                {
                    "clienteId" : 1,
                    "itens" : [null]
                }
                """;

        //Executar e Verificar
        mockMvc.perform(post("/vendas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(vendaService, never()).cadastrarVenda(any(VendaRequestDTO.class));

    }

    @Test
    void deveRetornar204aoDeletarVenda() throws Exception{

        //Executar e Verificar
        mockMvc.perform(delete("/vendas/{id}", 10))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(vendaService).deletarVenda(10L);
    }

    @Test
    void deveRetornar404aoDeletarVendaInexistente() throws Exception{

        //Preparar
        doThrow(new ResourceNotFoundException(99L)).when(vendaService).deletarVenda(99L);

        //Executar e Verificar
        mockMvc.perform(delete("/vendas/{id}", 99))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Recurso não encontrado"))
                .andExpect(jsonPath("$.caminho").value("/vendas/99"));

        verify(vendaService).deletarVenda(99L);

    }

    

}
