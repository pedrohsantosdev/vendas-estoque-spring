package com.example.vendasestoque.services;

import com.example.vendasestoque.dtos.ClienteRequestDTO;
import com.example.vendasestoque.entities.Cliente;
import com.example.vendasestoque.repositories.ClienteRepository;
import com.example.vendasestoque.services.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public List<Cliente> listarClientes() {
        return clienteRepository.findAll();
    }

    public Cliente buscarClientePorId(Long id) {
        Optional<Cliente> obj = clienteRepository.findById(id);
        return obj.orElseThrow(() -> new ResourceNotFoundException(id));
    }

    public Cliente cadastrarCliente(ClienteRequestDTO requestDTO) {

        Cliente obj = new Cliente(null, requestDTO.nome(), requestDTO.email(), requestDTO.telefone());

        return clienteRepository.save(obj);
    }

    @Transactional
    public Cliente atualizarCliente(Long id, ClienteRequestDTO requestDTO) {
        Cliente clienteAtual = buscarClientePorId(id);
        modificarCliente(clienteAtual, requestDTO);
        return clienteRepository.save(clienteAtual);
    }

    @Transactional
    public void deletarCliente(Long id) {
        Cliente obj = buscarClientePorId(id);
        clienteRepository.delete(obj);
    }

    private void modificarCliente(Cliente clienteAtual, ClienteRequestDTO requestDTO) {
        clienteAtual.setNome(requestDTO.nome());
        clienteAtual.setEmail(requestDTO.email());
        clienteAtual.setTelefone(requestDTO.telefone());
    }

}
