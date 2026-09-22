package com.example.vendasestoque.resources;

import com.example.vendasestoque.entities.MovimentacaoEstoque;
import com.example.vendasestoque.services.MovimentacaoEstoqueService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping(value = "/movimentacoes")
public class MovimentacaoEstoqueResource {

   private final MovimentacaoEstoqueService movimentacaoEstoqueService;

   public MovimentacaoEstoqueResource(MovimentacaoEstoqueService movimentacaoEstoqueService) {
        this.movimentacaoEstoqueService = movimentacaoEstoqueService;
   }

   @GetMapping
   public ResponseEntity<List<MovimentacaoEstoque>> listarMovimentacoes() {
       List<MovimentacaoEstoque> list = movimentacaoEstoqueService.listarMovimentacoes();
       return ResponseEntity.ok().body(list);
   }

   @GetMapping(value = "/{id}")
   public ResponseEntity<MovimentacaoEstoque> buscarMovimentacaoPorId(@PathVariable Long id) {
       MovimentacaoEstoque obj = movimentacaoEstoqueService.buscarMovimentacaoPorId(id);
       return ResponseEntity.ok().body(obj);
   }

   @PostMapping
   public ResponseEntity<MovimentacaoEstoque> cadastrarMovimentacao(@RequestBody MovimentacaoEstoque obj) {
       obj = movimentacaoEstoqueService.cadastrarMovimentacao(obj);

       URI uri = ServletUriComponentsBuilder
               .fromCurrentRequest()
               .path("/{id}")
               .buildAndExpand(obj.getId())
               .toUri();

       return ResponseEntity.created(uri).body(obj);
   }

   @PatchMapping(value = "/{id}")
   public ResponseEntity<MovimentacaoEstoque> atualizarMovimentacao(@PathVariable Long id, @RequestBody MovimentacaoEstoque obj) {
       obj = movimentacaoEstoqueService.atualizarMovimentacao(id, obj);
       return ResponseEntity.ok().body(obj);
   }
}
