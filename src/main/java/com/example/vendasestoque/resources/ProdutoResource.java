package com.example.vendasestoque.resources;

import com.example.vendasestoque.dtos.ProdutoRequestDTO;
import com.example.vendasestoque.dtos.ProdutoResponseDTO;
import com.example.vendasestoque.entities.Produto;
import com.example.vendasestoque.services.ProdutoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping(value = "/produtos")
public class ProdutoResource {

    private final ProdutoService produtoService;

    public ProdutoResource(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @GetMapping
    public ResponseEntity<List<ProdutoResponseDTO>> listarProdutos() {
        List<Produto> list = produtoService.listarProdutos();
        List<ProdutoResponseDTO> produtos = list.stream().
                map(produto -> new ProdutoResponseDTO(produto)).toList();
        return ResponseEntity.ok().body(produtos);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<ProdutoResponseDTO> buscarProdutoPorId(@PathVariable Long id) {
        Produto obj = produtoService.buscarProdutoPorId(id);
        return ResponseEntity.ok().body(new ProdutoResponseDTO(obj));
    }

    @GetMapping(value = "/codigo")
    public ResponseEntity<ProdutoResponseDTO> buscarProdutoPorCodigo(@RequestParam(name = "numerobarra") String codigo) {
        Produto obj = produtoService.buscarProdutoPorCodigo(codigo);
        return ResponseEntity.ok().body(new ProdutoResponseDTO(obj));
    }

    @GetMapping(value = "/baixoestoque")
    public ResponseEntity<List<ProdutoResponseDTO>> buscarProdutosComBaixoEstoque() {

        List<Produto> list = produtoService.buscarProdutosComEstoqueBaixo();
        List<ProdutoResponseDTO> produtos = list.stream()
                .map(produto -> new ProdutoResponseDTO(produto)).toList();

        return ResponseEntity.ok().body(produtos);
    }

    @PostMapping
    public ResponseEntity<ProdutoResponseDTO> cadastrarProduto(@Valid @RequestBody ProdutoRequestDTO requestDTO) {
        Produto produto = produtoService.cadastrarProduto(requestDTO);

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(produto.getId())
                .toUri();

        return ResponseEntity.created(uri).body(new ProdutoResponseDTO(produto));
    }

    @PutMapping(value = "/{id}")
    public ResponseEntity<ProdutoResponseDTO> atualizarProduto(@PathVariable Long id, @Valid @RequestBody ProdutoRequestDTO requestDTO) {
        Produto produto = produtoService.atualizarProduto(id, requestDTO);
        return ResponseEntity.ok().body(new ProdutoResponseDTO(produto));
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> deletarProduto(@PathVariable Long id) {
        produtoService.deletarProduto(id);
        return ResponseEntity.noContent().build();
    }
}
