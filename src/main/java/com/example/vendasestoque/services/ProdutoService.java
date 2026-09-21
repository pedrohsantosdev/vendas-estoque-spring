package com.example.vendasestoque.services;

import com.example.vendasestoque.entities.Produto;
import com.example.vendasestoque.repositories.ProdutoRepository;
import com.example.vendasestoque.services.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;

    public ProdutoService(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    public List<Produto> listarProdutos() {
        return produtoRepository.findAll();
    }

    public Produto buscarProdutoPorId(Long id) {
        Optional<Produto> obj = produtoRepository.findById(id);
        return obj.orElseThrow(() -> new ResourceNotFoundException(id));
    }

    public Produto buscarProdutoPorCodigo(String codigo) {
        return produtoRepository.findByCodigo(codigo);
    }

    public Produto cadastrarProduto(Produto obj) {
        return produtoRepository.save(obj);
    }

    @Transactional
    public Produto atualizarProduto(Long id, Produto novoProduto) {
        Produto produtoAtual = buscarProdutoPorId(id);
        modificarProduto(produtoAtual, novoProduto);
        return produtoRepository.save(produtoAtual);
    }

    @Transactional
    public void deletarProduto(Long id) {
        Produto obj = buscarProdutoPorId(id);
        produtoRepository.delete(obj);
    }

    private void modificarProduto(Produto produtoAtual, Produto novoProduto) {
        produtoAtual.setNome(novoProduto.getNome());
        produtoAtual.setCodigo(novoProduto.getCodigo());
        produtoAtual.setPreco(novoProduto.getPreco());
        produtoAtual.setEstoqueMinimo(novoProduto.getEstoqueMinimo());
    }
}
