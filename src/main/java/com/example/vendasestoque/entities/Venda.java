package com.example.vendasestoque.entities;

import com.example.vendasestoque.entities.enuns.StatusVenda;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.Instant;

@Entity
public class Venda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Instant momento;
    private StatusVenda statusVenda;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cliente outro)) return false;

        return getId() != null && getId().equals(outro.getId());
    }

    @Override
    public int hashCode() {
        return Cliente.class.hashCode();
    }
}
