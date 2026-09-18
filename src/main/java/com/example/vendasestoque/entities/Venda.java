package com.example.vendasestoque.entities;

import com.example.vendasestoque.entities.enuns.StatusVenda;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Venda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Instant momento;
    private StatusVenda statusVenda;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @OneToMany(mappedBy = "id.venda")
    private Set<ItemVenda> itens = new HashSet<>();

    public Venda() {
    }

    public Venda(Long id, Instant momento, StatusVenda statusVenda) {
        this.id = id;
        this.momento = momento;
        this.statusVenda = statusVenda;
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
        return statusVenda;
    }

    public void setStatusVenda(StatusVenda statusVenda) {
        this.statusVenda = statusVenda;
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
