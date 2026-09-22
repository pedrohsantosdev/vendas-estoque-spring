package com.example.vendasestoque.resources;

import com.example.vendasestoque.dtos.ClienteRequestDTO;
import com.example.vendasestoque.dtos.ClienteResponseDTO;
import com.example.vendasestoque.entities.Cliente;
import com.example.vendasestoque.services.ClienteService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping(value = "/clientes")
public class ClienteResource {

    private final ClienteService clienteService;

    public ClienteResource(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    public ResponseEntity<List<ClienteResponseDTO>> listarClientes() {
        List<Cliente> list = clienteService.listarClientes();
        List<ClienteResponseDTO> clientes = list.stream().map(cliente -> new ClienteResponseDTO(cliente)).toList();
        return ResponseEntity.ok().body(clientes);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<ClienteResponseDTO> buscarClientePorId(@PathVariable Long id) {
        Cliente obj = clienteService.buscarClientePorId(id);
        return ResponseEntity.ok().body(new ClienteResponseDTO(obj));
    }

    @PostMapping
    public ResponseEntity<ClienteResponseDTO> cadastrarCliente(@Valid @RequestBody ClienteRequestDTO clienteRequestDTO) {
        Cliente cliente = clienteService.cadastrarCliente(clienteRequestDTO);

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(cliente.getId())
                .toUri();

        return ResponseEntity.created(uri).body(new ClienteResponseDTO(cliente));
    }

    @PutMapping(value = "/{id}")
    public ResponseEntity<ClienteResponseDTO> atualizarCliente(@PathVariable Long id, @Valid @RequestBody ClienteRequestDTO requestDTO) {
        Cliente cliente = clienteService.atualizarCliente(id, requestDTO);
        return ResponseEntity.ok().body(new ClienteResponseDTO(cliente));
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> deletarCliente(@PathVariable Long id) {
        clienteService.deletarCliente(id);
        return ResponseEntity.noContent().build();
    }
}
