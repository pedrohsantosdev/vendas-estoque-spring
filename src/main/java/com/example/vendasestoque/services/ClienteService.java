package com.example.vendasestoque.services;

import com.example.vendasestoque.entities.Cliente;
import com.example.vendasestoque.repositories.ClienteRepository;
import org.springframework.stereotype.Service;

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
        return obj.get();
    }

    public Cliente cadastrarCliente(Cliente obj) {
        return clienteRepository.save(obj);
    }

    public Cliente atualizarCliente(Long id, Cliente novoCliente) {
        Cliente clienteAtual = buscarClientePorId(id);
        modificarCliente(clienteAtual, novoCliente);
        return clienteRepository.save(clienteAtual);
    }

    public void deletarCliente(Long id) {
        Cliente obj = buscarClientePorId(id);
        clienteRepository.delete(obj);
    }

    private void modificarCliente(Cliente clienteAtual, Cliente novoCliente) {
        clienteAtual.setNome(novoCliente.getNome());
        clienteAtual.setEmail(novoCliente.getEmail());
        clienteAtual.setTelefone(novoCliente.getTelefone());
    }

}
