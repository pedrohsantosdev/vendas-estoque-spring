package com.example.vendasestoque.resources;

import com.example.vendasestoque.entities.Produto;
import com.example.vendasestoque.services.ProdutoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/produtos")
public class ProdutoResource {

    private final ProdutoService produtoService;

    public ProdutoResource(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @GetMapping
    public ResponseEntity<List<Produto>> listarProdutos() {
        List<Produto> list = produtoService.listarProdutos();
        return ResponseEntity.ok().body(list);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<Produto> buscarProdutoPorId(@PathVariable Long id) {
        Produto obj = produtoService.buscarProdutoPorId(id);
        return ResponseEntity.ok().body(obj);
    }

    @GetMapping(value = "/codigo")
    public ResponseEntity<Produto> buscarProdutoPorCodigo(@RequestParam(name = "numerobarra") String codigo) {
        Produto obj = produtoService.buscarProdutoPorCodigo(codigo);
        return ResponseEntity.ok().body(obj);
    }

    @PostMapping
    public ResponseEntity<Produto> cadastrarProduto(@RequestBody Produto obj) {
        obj = produtoService.cadastrarProduto(obj);
        return ResponseEntity.ok().body(obj);
    }

    @PutMapping(value = "/{id}")
    public ResponseEntity<Produto> atualizarProduto(@PathVariable Long id, @RequestBody Produto obj) {
        obj = produtoService.atualizarProduto(id, obj);
        return ResponseEntity.ok().body(obj);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> deletarProduto(@PathVariable Long id) {
        produtoService.deletarProduto(id);
        return ResponseEntity.noContent().build();
    }
}
