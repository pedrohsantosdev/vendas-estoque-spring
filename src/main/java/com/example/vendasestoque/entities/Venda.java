package com.example.vendasestoque.entities;

import com.example.vendasestoque.entities.enuns.StatusVenda;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
public class Venda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Instant momento;
    private Integer statusVenda;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @OneToMany(mappedBy = "id.venda")
    private Set<ItemVenda> itens = new HashSet<>();

    @OneToMany(mappedBy = "venda")
    private List<MovimentacaoEstoque> movimentacoes = new ArrayList<>();

    public Venda() {
    }

    public Venda(Long id, Instant momento, StatusVenda statusVenda, Cliente cliente) {
        this.id = id;
        this.momento = momento;
        setStatusVenda(statusVenda);
        this.cliente = cliente;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getMomento() {
        return momento;
    }

    public void setMomento(Instant momento) {
        this.momento = momento;
    }

    public StatusVenda getStatusVenda() {
        return StatusVenda.valueOf(statusVenda);
    }

    public void setStatusVenda(StatusVenda statusVenda) {
        if(statusVenda != null) {
            this.statusVenda = statusVenda.getCode();
        }
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Set<ItemVenda> getItens() {
        return itens;
    }

    public List<MovimentacaoEstoque> getMovimentacoes() {
        return movimentacoes;
    }

    public BigDecimal getTotal() {
        BigDecimal soma = BigDecimal.ZERO;

        for (ItemVenda item : itens) {
            soma = soma.add(item.getSubTotal());
        }

        return soma;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Venda outro)) return false;

        return getId() != null && getId().equals(outro.getId());
    }

    @Override
    public int hashCode() {
        return Venda.class.hashCode();
    }
}
