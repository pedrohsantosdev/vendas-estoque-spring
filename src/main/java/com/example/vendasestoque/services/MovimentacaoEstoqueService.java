package com.example.vendasestoque.services;

import com.example.vendasestoque.entities.MovimentacaoEstoque;
import com.example.vendasestoque.repositories.MovimentacaoEstoqueRepository;
import com.example.vendasestoque.services.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class MovimentacaoEstoqueService {

    private final MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;

    public MovimentacaoEstoqueService(MovimentacaoEstoqueRepository movimentacaoEstoqueRepository) {
        this.movimentacaoEstoqueRepository = movimentacaoEstoqueRepository;
    }

    public List<MovimentacaoEstoque> listarMovimentacoes() {
        return movimentacaoEstoqueRepository.findAll();
    }

    public MovimentacaoEstoque buscarMovimentacaoPorId(Long id) {
        Optional<MovimentacaoEstoque> obj = movimentacaoEstoqueRepository.findById(id);
        return obj.orElseThrow(() -> new ResourceNotFoundException(id));
    }

    public MovimentacaoEstoque cadastrarMovimentacao(MovimentacaoEstoque movimentacaoEstoque) {
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
}
