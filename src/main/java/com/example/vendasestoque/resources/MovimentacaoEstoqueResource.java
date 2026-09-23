package com.example.vendasestoque.resources;

import com.example.vendasestoque.dtos.MovimentacaoRequestDTO;
import com.example.vendasestoque.dtos.MovimentacaoResponseDTO;
import com.example.vendasestoque.dtos.MovimentacaoUpdateDTO;
import com.example.vendasestoque.entities.MovimentacaoEstoque;
import com.example.vendasestoque.services.MovimentacaoEstoqueService;
import jakarta.validation.Valid;
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
   public ResponseEntity<List<MovimentacaoResponseDTO>> listarMovimentacoes() {
       List<MovimentacaoEstoque> list = movimentacaoEstoqueService.listarMovimentacoes();
       List<MovimentacaoResponseDTO> movimentacoes = list.stream().
               map(movimentacaoEstoque -> new MovimentacaoResponseDTO(movimentacaoEstoque)).toList();
       return ResponseEntity.ok().body(movimentacoes);
   }

   @GetMapping(value = "/{id}")
   public ResponseEntity<MovimentacaoResponseDTO> buscarMovimentacaoPorId(@PathVariable Long id) {
       MovimentacaoEstoque obj = movimentacaoEstoqueService.buscarMovimentacaoPorId(id);
       return ResponseEntity.ok().body(new MovimentacaoResponseDTO(obj));
   }

   @PostMapping
   public ResponseEntity<MovimentacaoResponseDTO> cadastrarMovimentacao(@Valid @RequestBody MovimentacaoRequestDTO requestDTO) {
       MovimentacaoEstoque movimentacaoEstoque = movimentacaoEstoqueService.cadastrarMovimentacao(requestDTO);

       URI uri = ServletUriComponentsBuilder
               .fromCurrentRequest()
               .path("/{id}")
               .buildAndExpand(movimentacaoEstoque.getId())
               .toUri();

       return ResponseEntity.created(uri).body(new MovimentacaoResponseDTO(movimentacaoEstoque));
   }

   @PatchMapping(value = "/{id}")
   public ResponseEntity<MovimentacaoResponseDTO> atualizarMovimentacao(@PathVariable Long id, @Valid @RequestBody MovimentacaoUpdateDTO updateDTO) {
       MovimentacaoEstoque movimentacaoEstoque = movimentacaoEstoqueService.atualizarMovimentacao(id, updateDTO);
       return ResponseEntity.ok().body(new MovimentacaoResponseDTO(movimentacaoEstoque));
   }
}
