package com.example.vendasestoque.services;

import com.example.vendasestoque.entities.MovimentacaoEstoque;
import com.example.vendasestoque.entities.Produto;
import com.example.vendasestoque.repositories.MovimentacaoEstoqueRepository;
import com.example.vendasestoque.services.exceptions.EstoqueInsuficiente;
import com.example.vendasestoque.services.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class MovimentacaoEstoqueService {

    private final MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;
    private final ProdutoService produtoService;

    public MovimentacaoEstoqueService(MovimentacaoEstoqueRepository movimentacaoEstoqueRepository, ProdutoService produtoService) {
        this.movimentacaoEstoqueRepository = movimentacaoEstoqueRepository;
        this.produtoService = produtoService;
    }

    public List<MovimentacaoEstoque> listarMovimentacoes() {
        return movimentacaoEstoqueRepository.findAll();
    }

    public MovimentacaoEstoque buscarMovimentacaoPorId(Long id) {
        Optional<MovimentacaoEstoque> obj = movimentacaoEstoqueRepository.findById(id);
        return obj.orElseThrow(() -> new ResourceNotFoundException(id));
    }

    @Transactional
    public MovimentacaoEstoque cadastrarMovimentacao(MovimentacaoEstoque movimentacaoEstoque) {

        Produto produto = produtoService.buscarProdutoPorId(movimentacaoEstoque.getProduto().getId());

        Integer quantidade = movimentacaoEstoque.getQuantidade();

        if (quantidade == null || quantidade <= 0) {
            throw new IllegalArgumentException(
                    "A quantidade deve ser maior que zero"
            );
        }

        if(movimentacaoEstoque.getTipoMovimentacao().getCode() == 1) {
            inserirEstoque(produto, quantidade);
        }

        if(movimentacaoEstoque.getTipoMovimentacao().getCode() == 2) {
            validarSaidaDeEstoque(movimentacaoEstoque, produto);
            retirarEstoque(produto, quantidade);
        }

        movimentacaoEstoque.setProduto(produto);

        return movimentacaoEstoqueRepository.save(movimentacaoEstoque);
    }

    @Transactional
    public MovimentacaoEstoque atualizarMovimentacao(Long id, MovimentacaoEstoque novaMovimentacao) {
        MovimentacaoEstoque movimentacaoAtual = buscarMovimentacaoPorId(id);
        modificarMovimentacao(movimentacaoAtual, novaMovimentacao);
        return movimentacaoEstoqueRepository.save(movimentacaoAtual);
    }

    private void modificarMovimentacao(MovimentacaoEstoque movimentacaoAtual, MovimentacaoEstoque novaMovimentacao) {
        movimentacaoAtual.setMotivo(novaMovimentacao.getMotivo());
    }

    private void inserirEstoque(Produto produto, Integer quantidade) {
        int novoEstoque = produto.getQuantidadeEstoque() + quantidade;
        produto.setQuantidadeEstoque(novoEstoque);
    }

    private void retirarEstoque(Produto produto, Integer quantidade) {
        int novoEstoque = produto.getQuantidadeEstoque() - quantidade;
        produto.setQuantidadeEstoque(novoEstoque);
    }

    private void validarSaidaDeEstoque(MovimentacaoEstoque movimentacaoEstoque, Produto produto) {
        if(movimentacaoEstoque.getQuantidade() > produto.getQuantidadeEstoque() ) {
            throw new EstoqueInsuficiente("Estoque do produto insufiente: " + produto.getNome());
        }
    }

}
