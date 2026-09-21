package com.example.vendasestoque.services;

import com.example.vendasestoque.dtos.VendaRequestDTO;
import com.example.vendasestoque.entities.*;
import com.example.vendasestoque.entities.PK.ItemVendaPK;
import com.example.vendasestoque.entities.enuns.StatusVenda;
import com.example.vendasestoque.entities.enuns.TipoMovimentacao;
import com.example.vendasestoque.repositories.ItemVendaRepository;
import com.example.vendasestoque.repositories.VendaRepository;
import com.example.vendasestoque.services.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class VendaService {

    private final VendaRepository vendaRepository;
    private final ClienteService clienteService;
    private final ProdutoService produtoService;
    private final ItemVendaRepository itemVendaRepository;
    private final MovimentacaoEstoqueService movimentacaoEstoqueService;

    public VendaService(VendaRepository vendaRepository, ClienteService clienteService, ProdutoService produtoService, ItemVendaRepository itemVendaRepository, MovimentacaoEstoqueService movimentacaoEstoqueService) {
        this.vendaRepository = vendaRepository;
        this.clienteService = clienteService;
        this.produtoService = produtoService;
        this.itemVendaRepository = itemVendaRepository;
        this.movimentacaoEstoqueService = movimentacaoEstoqueService;
    }

    public List<Venda> listarVendas() {
        return vendaRepository.findAll();
    }

    public Venda buscarVendaPorId(Long id) {
        Optional<Venda> obj = vendaRepository.findById(id);
        return obj.orElseThrow(() -> new ResourceNotFoundException(id));
    }

    @Transactional
    public Venda cadastrarVenda(VendaRequestDTO requestDTO) {

        Cliente cliente = clienteService.buscarClientePorId(requestDTO.clienteId());
        Produto produto = produtoService.buscarProdutoPorId(requestDTO.produtoId());

        Venda venda = new Venda(null, Instant.now(), StatusVenda.CONFIRMADA, cliente);

        venda = vendaRepository.save(venda);

        ItemVenda itemVenda = new ItemVenda(new ItemVendaPK(venda, produto), requestDTO.quantidade(), produto.getPreco());

        itemVendaRepository.save(itemVenda);
        venda.getItens().add(itemVenda);

        MovimentacaoEstoque movimentacaoEstoque = new MovimentacaoEstoque(
                null, TipoMovimentacao.SAIDA, requestDTO.quantidade(), venda.getMomento(),
                "Venda de " + produto.getNome(), produto, venda
        );

        movimentacaoEstoqueService.cadastrarMovimentacao(movimentacaoEstoque);

        return venda;
    }

    @Transactional
    public Venda cancelarVenda(Long id) {
        Venda obj = buscarVendaPorId(id);
        obj.setStatusVenda(StatusVenda.CANCELADA);
        return vendaRepository.save(obj);
    }

    @Transactional
    public void deletarVenda(Long id) {
        Venda obj = buscarVendaPorId(id);
        vendaRepository.delete(obj);
    }
}
