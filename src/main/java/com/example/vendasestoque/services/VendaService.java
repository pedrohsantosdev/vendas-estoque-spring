package com.example.vendasestoque.services;

import com.example.vendasestoque.entities.Venda;
import com.example.vendasestoque.entities.enuns.StatusVenda;
import com.example.vendasestoque.repositories.VendaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VendaService {

    private final VendaRepository vendaRepository;

    public VendaService(VendaRepository vendaRepository) {
        this.vendaRepository = vendaRepository;
    }

    public List<Venda> listarVendas() {
        return vendaRepository.findAll();
    }

    public Venda buscarVendaPorId(Long id) {
        Optional<Venda> obj = vendaRepository.findById(id);
        return obj.get();
    }

    public Venda cadastrarVenda(Venda obj) {
        return vendaRepository.save(obj);
    }

    public Venda cancelarVenda(Long id) {
        Venda obj = buscarVendaPorId(id);
        obj.setStatusVenda(StatusVenda.CANCELADA);
        return vendaRepository.save(obj);
    }

    public void deletarVenda(Long id) {
        Venda obj = buscarVendaPorId(id);
        vendaRepository.delete(obj);
    }
}
