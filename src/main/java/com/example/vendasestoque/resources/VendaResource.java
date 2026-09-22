package com.example.vendasestoque.resources;

import com.example.vendasestoque.dtos.VendaRequestDTO;
import com.example.vendasestoque.entities.Venda;
import com.example.vendasestoque.services.VendaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping(value = "/vendas")
public class VendaResource {

    private final VendaService vendaService;

    public VendaResource(VendaService vendaService) {
        this.vendaService = vendaService;
    }

    @GetMapping
    public ResponseEntity<List<Venda>> listarVendas() {
        List<Venda> list = vendaService.listarVendas();
        return ResponseEntity.ok().body(list);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<Venda> buscarVendaPorId(@PathVariable Long id) {
        Venda obj = vendaService.buscarVendaPorId(id);
        return ResponseEntity.ok().body(obj);
    }

    @PostMapping
    public ResponseEntity<Venda> cadastrarVenda(@RequestBody VendaRequestDTO requestDTO) {
        Venda obj = vendaService.cadastrarVenda(requestDTO);

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(obj.getId())
                .toUri();

        return ResponseEntity.created(uri).body(obj);
    }

    @PatchMapping(value = "/{id}")
    public ResponseEntity<Venda> cancelarVenda(@PathVariable Long id) {
        Venda obj = vendaService.cancelarVenda(id);
        return ResponseEntity.ok().body(obj);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> deletarVenda(@PathVariable Long id) {
        vendaService.deletarVenda(id);
        return ResponseEntity.noContent().build();
    }
}
