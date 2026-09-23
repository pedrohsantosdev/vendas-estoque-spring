package com.example.vendasestoque.services;

import com.example.vendasestoque.dtos.MovimentacaoRequestDTO;
import com.example.vendasestoque.dtos.MovimentacaoUpdateDTO;
import com.example.vendasestoque.entities.MovimentacaoEstoque;
import com.example.vendasestoque.entities.Produto;
import com.example.vendasestoque.repositories.MovimentacaoEstoqueRepository;
import com.example.vendasestoque.services.exceptions.EstoqueInsuficiente;
import com.example.vendasestoque.services.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
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

    //Venda ou cancelamento
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

    //Requisição de reposição
    @Transactional
    public MovimentacaoEstoque cadastrarMovimentacao(MovimentacaoRequestDTO requestDTO) {

        Produto produto = produtoService.buscarProdutoPorId(requestDTO.produtoId());

        MovimentacaoEstoque movimentacaoEstoque = new MovimentacaoEstoque(
                null,
                requestDTO.tipoMovimentacao(),
                requestDTO.quantidade(),
                Instant.now(),
                requestDTO.motivo(),
                produto,
                null
        );

        return cadastrarMovimentacao(movimentacaoEstoque);
    }

    @Transactional
    public MovimentacaoEstoque atualizarMovimentacao(Long id, MovimentacaoUpdateDTO updateDTO) {
        MovimentacaoEstoque movimentacaoAtual = buscarMovimentacaoPorId(id);
        modificarMovimentacao(movimentacaoAtual, updateDTO);
        return movimentacaoEstoqueRepository.save(movimentacaoAtual);
    }

    private void modificarMovimentacao(MovimentacaoEstoque movimentacaoAtual, MovimentacaoUpdateDTO updateDTO) {
        movimentacaoAtual.setMotivo(updateDTO.motivo());
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
        if( movimentacaoEstoque.getQuantidade() > produto.getQuantidadeEstoque() ) {
            throw new EstoqueInsuficiente("Estoque do produto insufiente: " + produto.getNome());
        }
    }

}
