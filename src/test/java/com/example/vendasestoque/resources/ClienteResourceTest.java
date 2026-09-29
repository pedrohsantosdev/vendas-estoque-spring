package com.example.vendasestoque.resources;

import com.example.vendasestoque.dtos.ClienteRequestDTO;
import com.example.vendasestoque.entities.Cliente;
import com.example.vendasestoque.services.ClienteService;
import com.example.vendasestoque.services.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClienteResource.class)
@ActiveProfiles("Test")
public class ClienteResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClienteService clienteService;

    @Test
    void deveRetornar200comListaDeClientesCadastrados() throws Exception {

        //Preparar
        Cliente c1 = new Cliente(
                1L, "Maria Silva",
                "maria@example.com", "11987654321"
        );

        Cliente c2 = new Cliente(
                2L, "João Souza",
                "joao@example.com", "21987654321"
        );

        when(clienteService.listarClientes()).thenReturn(List.of(c1, c2));

        //Executar e Verificar
        mockMvc.perform(get("/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(clienteService).listarClientes();
    }

    @Test
    void deveRetornar200semClienteCadastrados() throws Exception {

        //Preparar
        when(clienteService.listarClientes()).thenReturn(List.of());

        //Executar e Verificar
        mockMvc.perform(get("/clientes"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(clienteService).listarClientes();

    }

    @Test
    void deveRetornar200paraClienteComIdExistente() throws Exception {

        //Preparar
        Cliente c1 = new Cliente(
                1L, "Maria Silva",
                "maria@example.com", "11987654321"
        );

        when(clienteService.buscarClientePorId(1L)).thenReturn(c1);

        //Executar e Verificar
        mockMvc.perform(get("/clientes/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Maria Silva"));

        verify(clienteService).buscarClientePorId(1L);
    }

    @Test
    void deveRetornar404paraClienteInexistente() throws Exception {

        //Preparar
        when(clienteService.buscarClientePorId(99L)).thenThrow(new ResourceNotFoundException(99L));

        //Executar e Verificar
        mockMvc.perform(get("/clientes/{id}", 99))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Recurso não encontrado"))
                .andExpect(jsonPath("$.caminho").value("/clientes/99"));

        verify(clienteService).buscarClientePorId(99L);

    }

    @Test
    void deveRetornar201paraCadastrarClienteValido() throws Exception {

        //Preparar
        Cliente c1 = new Cliente(
                10L, "Maria Silva",
                "maria@example.com", "11987654321"
        );

        when(clienteService.cadastrarCliente(any(ClienteRequestDTO.class))).thenReturn(c1);

        String json =
                """
                                {
                                    "nome" : "Maria Silva",
                                    "email": "maria@example.com",
                                    "telefone": "11987654321"
                                }
                        """;

        //Executar e Verificar
        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location", "http://localhost/clientes/10"))
                .andExpect(jsonPath("$.id").value(10));

        ArgumentCaptor<ClienteRequestDTO> captor = ArgumentCaptor.forClass(ClienteRequestDTO.class);
        verify(clienteService).cadastrarCliente(captor.capture());
        ClienteRequestDTO clienteRequestDTO = captor.getValue();

        assertEquals("Maria Silva", clienteRequestDTO.nome());
        assertEquals("maria@example.com", clienteRequestDTO.email());
        assertEquals("11987654321", clienteRequestDTO.telefone());

    }

    @Test
    void retornar400aoCadastrarComNomeVazio() throws Exception {

        //Preparar
        String json =
                """
                        {
                            "nome" : "",
                            "email": "maria@example.com",
                            "telefone": "11987654321"
                        }
                        """;

        //Executar e Preparar
        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(clienteService, never()).cadastrarCliente(any(ClienteRequestDTO.class));
    }

    @Test
    void retornar400aoCadastrarComNomeNull() throws Exception {

        //Preparar
        String json =
                """
                        {
                            "nome" : null,
                            "email": "maria@example.com",
                            "telefone": "11987654321"
                        }
                        """;

        //Executar e Preparar
        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(clienteService, never()).cadastrarCliente(any(ClienteRequestDTO.class));
    }

    @Test
    void retornar400aoCadastrarNomeSomenteComEspacos() throws Exception {

        //Preparar
        String json =
                """
                        {
                            "nome" : "    ",
                            "email": "maria@example.com",
                            "telefone": "11987654321"
                        }
                        """;

        //Executar e Preparar
        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(clienteService, never()).cadastrarCliente(any(ClienteRequestDTO.class));
    }

    @Test
    void deveRetornar400paraEmailVazio() throws Exception {

        //Preparar
        String json =
                """
                        {
                        "nome" : "Maria Silva",
                        "email": "",
                        "telefone": "11987654321"
                        }
                        """;

        //Executar e Verificar
        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(clienteService, never()).cadastrarCliente(any(ClienteRequestDTO.class));
    }

    @Test
    void deveRetornar400paraEmailNulo() throws Exception {

        //Preparar
        String json =
                """
                        {
                        "nome" : "Maria Silva",
                        "email": null,
                        "telefone": "11987654321"
                        }
                        """;

        //Executar e Verificar
        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(clienteService, never()).cadastrarCliente(any(ClienteRequestDTO.class));
    }

    @Test
    void deveRetornar400paraTelefoneVazio() throws Exception {

        //Preparar
        String json =
                """
                        {
                        "nome" : "Maria Silva",
                        "email": "maria@exemple.com",
                        "telefone": ""
                        }
                        """;

        //Executar e Verificar
        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(clienteService, never()).cadastrarCliente(any(ClienteRequestDTO.class));
    }

    @Test
    void deveRetornar400paraTelefoneSomenteComEspacos() throws Exception {

        //Preparar
        String json =
                """
                        {
                        "nome" : "Maria Silva",
                        "email": "maria@exemple.com",
                        "telefone": "   "
                        }
                        """;

        //Executar e Verificar
        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(clienteService, never()).cadastrarCliente(any(ClienteRequestDTO.class));
    }

    @Test
    void deveRetornar400paraTelefoneNulo() throws Exception {

        //Preparar
        String json =
                """
                        {
                        "nome" : "Maria Silva",
                        "email": "maria@exemple.com",
                        "telefone": null
                        }
                        """;

        //Executar e Verificar
        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(clienteService, never()).cadastrarCliente(any(ClienteRequestDTO.class));
    }

    @Test
    void deveRetornar200aoAtualizarClienteExistenteComDadosValidos() throws Exception {

        //Preparar
        Cliente clienteAtualizado = new Cliente(
                1L, "João Souza",
                "joao@example.com", "21987654321");

        String json =
                """
                        {
                        "nome" : "João Souza",
                        "email": "joao@example.com",
                        "telefone": "21987654321"
                        }
                        """;

        when(clienteService.atualizarCliente(eq(1L), any(ClienteRequestDTO.class)))
                .thenReturn(clienteAtualizado);

        //Executar e Verificar
        mockMvc.perform(put("/clientes/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        ArgumentCaptor<ClienteRequestDTO> captor = ArgumentCaptor.forClass(ClienteRequestDTO.class);
        verify(clienteService).atualizarCliente(eq(1L), captor.capture());
        ClienteRequestDTO  clienteRequestDTO = captor.getValue();

        assertEquals("João Souza", clienteRequestDTO.nome());
        assertEquals("joao@example.com", clienteRequestDTO.email());
        assertEquals("21987654321", clienteRequestDTO.telefone());

    }

    @Test
    void deveRetornar404aoAtualizarClienteInexistente() throws Exception {

        //Preparar
        String json =
                """
                        {
                        "nome" : "João Souza",
                        "email": "joao@example.com",
                        "telefone": "21987654321"
                        }
                        """;

        when(clienteService.atualizarCliente(eq(99L), any(ClienteRequestDTO.class))).thenThrow(new ResourceNotFoundException(99L));

        //Executar e Verificar
        mockMvc.perform(put("/clientes/{id}", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());

        verify(clienteService).atualizarCliente(eq(99L), any(ClienteRequestDTO.class));

    }

    @Test
    void deveRetornar400aoAtualizarClienteNomeVazio() throws Exception {

        //Preparar
        String json =
                """
                        {
                        "nome" : "",
                        "email": "joao@example.com",
                        "telefone": "21987654321"
                        }
                        """;

        //Executar e Verificar
        mockMvc.perform(put("/clientes/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());

        verify(clienteService, never()).atualizarCliente(anyLong(), any(ClienteRequestDTO.class));

    }

    @Test
    void deveRetornar400aoAtualizarClienteNomeSometeComEspacos() throws Exception {

        //Preparar
        String json =
                """
                        {
                        "nome" : "  ",
                        "email": "joao@example.com",
                        "telefone": "21987654321"
                        }
                        """;

        //Executar e Verificar
        mockMvc.perform(put("/clientes/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(clienteService, never()).atualizarCliente(anyLong(), any(ClienteRequestDTO.class));

    }

    @Test
    void deveRetornar400aoAtualizarClienteNomeNulo() throws Exception {

        //Preparar
        String json =
                """
                        {
                        "nome" : null,
                        "email": "joao@example.com",
                        "telefone": "21987654321"
                        }
                        """;

        //Executar e Verificar
        mockMvc.perform(put("/clientes/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(clienteService, never()).atualizarCliente(anyLong(), any(ClienteRequestDTO.class));

    }

    @Test
    void deveRetornar400aoAtualizarClienteComEmailVazio() throws Exception {

        //Preparar
        String json =
                """
                        {
                        "nome" : "João Souza",
                        "email": "",
                        "telefone": "21987654321"
                        }
                        """;

        //Executar e Verificar
        mockMvc.perform(put("/clientes/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(clienteService, never()).atualizarCliente(anyLong(), any(ClienteRequestDTO.class));

    }

    @Test
    void deveRetornar400aoAtualizarClienteComEmailSomenteComEspacos() throws Exception {

        //Preparar
        String json =
                """
                        {
                        "nome" : "João Souza",
                        "email": "  ",
                        "telefone": "21987654321"
                        }
                        """;

        //Executar e Verificar
        mockMvc.perform(put("/clientes/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(clienteService, never()).atualizarCliente(anyLong(), any(ClienteRequestDTO.class));

    }

    @Test
    void deveRetornar400aoAtualizarClienteComEmailNulo() throws Exception {

        //Preparar
        String json =
                """
                        {
                        "nome" : "João Souza",
                        "email": null,
                        "telefone": "21987654321"
                        }
                        """;

        //Executar e Verificar
        mockMvc.perform(put("/clientes/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(clienteService, never()).atualizarCliente(anyLong(), any(ClienteRequestDTO.class));

    }

    @Test
    void deveRetornar400aoAtualizarClienteComTelefoneVazio() throws Exception {

        //Preparar
        String json =
                """
                        {
                        "nome" : "João Souza",
                        "email": "joao@example.com",
                        "telefone": ""
                        }
                        """;

        //Executar e Verificar
        mockMvc.perform(put("/clientes/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(clienteService, never()).atualizarCliente(anyLong(), any(ClienteRequestDTO.class));

    }

    @Test
    void deveRetornar400aoAtualizarClienteComTelefoneSomenteComEspacos() throws Exception {

        //Preparar
        String json =
                """
                        {
                        "nome" : "João Souza",
                        "email": "joao@example.com",
                        "telefone": "  "
                        }
                        """;

        //Executar e Verificar
        mockMvc.perform(put("/clientes/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(clienteService, never()).atualizarCliente(anyLong(), any(ClienteRequestDTO.class));

    }

    @Test
    void deveRetornar400aoAtualizarClienteComTelefoneNulo() throws Exception {

        //Preparar
        String json =
                """
                        {
                        "nome" : "João Souza",
                        "email": "joao@example.com",
                        "telefone": null
                        }
                        """;

        //Executar e Verificar
        mockMvc.perform(put("/clientes/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(clienteService, never()).atualizarCliente(anyLong(), any(ClienteRequestDTO.class));

    }

    @Test
    void deveRetornar204aoDeletarCliente() throws Exception{

        //Executar e Verificar
        mockMvc.perform(delete("/clientes/{id}", 10L))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(clienteService).deletarCliente(10L);
    }

    @Test
    void deveRetornar404aoExcluirClienteInexistente() throws Exception{

        //Preparar
        doThrow(new ResourceNotFoundException(99L)).when(clienteService).deletarCliente(99L);

        //Executar e Verifcar
        mockMvc.perform(delete("/clientes/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Recurso não encontrado"))
                .andExpect(jsonPath("$.caminho").value("/clientes/99"));

        verify(clienteService).deletarCliente(99L);
    }
}
