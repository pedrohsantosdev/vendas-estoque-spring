package com.example.vendasestoque.resources;

import com.example.vendasestoque.dtos.VendaRequestDTO;
import com.example.vendasestoque.services.VendaService;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VendaResource.class)
@ActiveProfiles("test")
public class VendaResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VendaService vendaService;

    @Test
    void deveRetornar400quandoClienteIdNaoForInformado() throws Exception{

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
    void deveRetornar400paraQuantidadeDeItens0() throws Exception{

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

    
}
