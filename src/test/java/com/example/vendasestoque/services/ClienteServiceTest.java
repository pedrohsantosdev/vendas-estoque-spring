package com.example.vendasestoque.services;

import com.example.vendasestoque.dtos.ClienteRequestDTO;
import com.example.vendasestoque.entities.Cliente;
import com.example.vendasestoque.repositories.ClienteRepository;
import com.example.vendasestoque.services.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ClienteService clienteService;

    @Test
    void deveListarClientes() {

        //Preparar
        Cliente c1 = new Cliente(
                null, "Maria Silva",
                "maria@example.com", "11987654321"
        );

        Cliente c2 = new Cliente(
                null, "João Souza",
                "joao@example.com", "21987654321"
        );

        when(clienteRepository.findAll()).thenReturn(List.of(c1, c2));

        //Executar
        List<Cliente> resultado = clienteService.listarClientes();

        //Verificar
        assertEquals(2, resultado.size());
        assertSame(c1, resultado.get(0));
        assertSame(c2, resultado.get(1));

    }

    @Test
    void deveRetornarListaVazia() {

        //Preparar
        when(clienteRepository.findAll()).thenReturn(List.of());

        //Executar
        List<Cliente> resultado = clienteService.listarClientes();

        //Verificar
        assertTrue(resultado.isEmpty());
        verify(clienteRepository).findAll();

    }

    @Test
    void deveBuscarClienteComIdExistente() {

        //Preparar
        Cliente c1 = new Cliente(
                1L, "Maria Silva",
                "maria@example.com", "11987654321"
        );

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(c1));

        //Executar
        Cliente resultado = clienteService.buscarClientePorId(1L);

        //Verificar
        assertSame(c1, resultado);
        verify(clienteRepository).findById(1L);

    }

    @Test
    void deveLancarExcecaoQuandoBuscarClienteComIdInexistente() {

        //Preparar
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        //Executar e Verificar
        assertThrows(
                ResourceNotFoundException.class,
                () -> clienteService.buscarClientePorId(99L)
        );

        verify(clienteRepository).findById(99L);

    }

    @Test
    void deveCadastrarCliente() {

        //Preparar
        ClienteRequestDTO clienteRequestDTO = new ClienteRequestDTO("Maria Silva",
                "maria@example.com", "11987654321");

        when(clienteRepository.save(any(Cliente.class))).thenAnswer(chamada -> chamada.getArgument(0));
        Cliente clienteResultado = clienteService.cadastrarCliente(clienteRequestDTO);

        //Executar
        ArgumentCaptor<Cliente> captor = ArgumentCaptor.forClass(Cliente.class);
        verify(clienteRepository).save(captor.capture());
        Cliente clienteCapturado = captor.getValue();

        //Verificar
        assertSame(clienteResultado, clienteCapturado);
        assertEquals("Maria Silva", clienteCapturado.getNome());
        assertEquals("maria@example.com", clienteCapturado.getEmail());
        assertEquals("11987654321", clienteCapturado.getTelefone());

    }

    @Test
    void deveAtualizarClienteExistente() {

        //Preparar
        Cliente c1 = new Cliente(
                1L, "Maria Silva",
                "maria@example.com", "11987654321"
        );

        ClienteRequestDTO dto = new ClienteRequestDTO(
                "Maria Souza",
                "maria.souza@example.com",
                "21999998888"
        );

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(c1));

        //Executar
        clienteService.atualizarCliente(1L, dto);

        // Verificar
        assertEquals(1L, c1.getId());
        assertEquals("Maria Souza", c1.getNome());
        assertEquals("maria.souza@example.com", c1.getEmail());
        assertEquals("21999998888", c1.getTelefone());

        verify(clienteRepository).save(c1);

    }

    @Test
    void deveRejeitarAtualizarClienteComIdInexistente() {

        //Preparar
        ClienteRequestDTO dto = new ClienteRequestDTO(
                "Maria Silva",
                "maria@example.com",
                "11987654321"
        );

        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        //Executar
        assertThrows(
                ResourceNotFoundException.class,
                () -> clienteService.atualizarCliente(99L, dto)
        );

        //Verificar
        verify(clienteRepository, never()).save(any(Cliente.class));

    }

    @Test
    void deveDeletarClienteComIdExistente() {

        //Preparar
        Cliente c1 = new Cliente(
                1L, "Maria Silva",
                "maria@example.com", "11987654321"
        );

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(c1));

        //Executar
        clienteService.deletarCliente(1L);

        //Verificar
        verify(clienteRepository).delete(c1);

    }

    @Test
    void deveRejeitarDeletarClienteComIdInexistente() {

        //Preparar
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        //Executar
        assertThrows(
                ResourceNotFoundException.class,
                () -> clienteService.deletarCliente(99L)
        );

        //Verificar
        verify(clienteRepository, never()).delete(any(Cliente.class));

    }

}
