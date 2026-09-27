package com.example.vendasestoque.services;

import com.example.vendasestoque.dtos.ItemVendaRequestDTO;
import com.example.vendasestoque.dtos.VendaRequestDTO;
import com.example.vendasestoque.entities.*;
import com.example.vendasestoque.entities.PK.ItemVendaPK;
import com.example.vendasestoque.entities.enuns.StatusVenda;
import com.example.vendasestoque.entities.enuns.TipoMovimentacao;
import com.example.vendasestoque.repositories.ItemVendaRepository;
import com.example.vendasestoque.repositories.VendaRepository;
import com.example.vendasestoque.services.exceptions.ItemRepetidoNaCompra;
import com.example.vendasestoque.services.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

        Venda venda = new Venda(null, Instant.now(), StatusVenda.CONFIRMADA, cliente);

        venda = vendaRepository.save(venda);

        Set<Long> produtosEncontrados = new HashSet<>();

        for(ItemVendaRequestDTO itemVenda : requestDTO.itens()) {

            boolean produtoAdicionado = produtosEncontrados.add(itemVenda.produtoId());

            if(!produtoAdicionado) {
                throw new ItemRepetidoNaCompra("Item repetido na compra, verifique!");
            }

            Produto produto = produtoService.buscarProdutoPorId(itemVenda.produtoId());
            Integer quantidade = itemVenda.quantidade();

            ItemVenda item = new ItemVenda(new ItemVendaPK(venda, produto), quantidade, produto.getPreco());

            item = itemVendaRepository.save(item);

            venda.getItens().add(item);

            MovimentacaoEstoque movimentacaoEstoque = new MovimentacaoEstoque(null, TipoMovimentacao.SAIDA, itemVenda.quantidade(), venda.getMomento(),
                    "Venda do produto " + produto.getNome(), produto, venda);

            movimentacaoEstoqueService.cadastrarMovimentacao(movimentacaoEstoque);
        }

        return venda;
    }

    @Transactional
    public Venda cancelarVenda(Long id) {

        Venda obj = buscarVendaPorId(id);

        if(obj.getStatusVenda() == StatusVenda.CANCELADA) {
            return obj;
        }

        for(ItemVenda item : obj.getItens()) {

            Produto produto = item.getId().getProduto();

            MovimentacaoEstoque movimentacaoEstoque = new MovimentacaoEstoque(
                    null, TipoMovimentacao.ENTRADA, item.getQuantidade(), Instant.now(),
                    "Cancelamento da venda " + obj.getId(),
                    produto, obj
            );

            movimentacaoEstoqueService.cadastrarMovimentacao(movimentacaoEstoque);

        }

        obj.setStatusVenda(StatusVenda.CANCELADA);

        return vendaRepository.save(obj);
    }

    @Transactional
    public void deletarVenda(Long id) {
        Venda obj = buscarVendaPorId(id);
        vendaRepository.delete(obj);
    }
}
