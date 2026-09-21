package com.example.vendasestoque.entities;

import com.example.vendasestoque.entities.enuns.TipoMovimentacao;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
public class MovimentacaoEstoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Integer tipoMovimentacao;
    private Integer quantidade;
    private Instant momento;
    private String motivo;

    @ManyToOne
    @JoinColumn(name = "produto_id")
    private Produto produto;

    @ManyToOne
    @JoinColumn(name = "venda_id")
    private Venda venda;

    public MovimentacaoEstoque() {
    }

    public MovimentacaoEstoque(Long id, TipoMovimentacao tipoMovimentacao, Integer quantidade, Instant momento, String motivo, Produto produto, Venda venda) {
        this.id = id;
        setTipoMovimentacao(tipoMovimentacao);
        this.quantidade = quantidade;
        this.momento = momento;
        this.motivo = motivo;
        this.produto = produto;
        this.venda = venda;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TipoMovimentacao getTipoMovimentacao() {
        return TipoMovimentacao.valueOf(tipoMovimentacao);
    }

    public void setTipoMovimentacao(TipoMovimentacao tipoMovimentacao) {

        if(tipoMovimentacao != null) {
            this.tipoMovimentacao = tipoMovimentacao.getCode();
        }
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public Instant getMomento() {
        return momento;
    }

    public void setMomento(Instant momento) {
        this.momento = momento;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    public Venda getVenda() {
        return venda;
    }

    public void setVenda(Venda venda) {
        this.venda = venda;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MovimentacaoEstoque outro)) return false;

        return getId() != null && getId().equals(outro.getId());
    }

    @Override
    public int hashCode() {
        return MovimentacaoEstoque.class.hashCode();
    }
}
