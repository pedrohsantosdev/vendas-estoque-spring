package com.example.vendasestoque.entities;

import com.example.vendasestoque.entities.PK.ItemVendaPK;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
public class ItemVenda {

    @EmbeddedId
    private ItemVendaPK id = new ItemVendaPK();

    private Integer quantidade;
    private BigDecimal precoUnitario;

    public ItemVenda() {
    }

    public ItemVenda(ItemVendaPK id, Integer quantidade, BigDecimal precoUnitario) {
        this.id = id;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
    }

    public ItemVendaPK getId() {
        return id;
    }

    public void setId(ItemVendaPK id) {
        this.id = id;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public BigDecimal getPrecoUnitario() {
        return precoUnitario;
    }

    public void setPrecoUnitario(BigDecimal precoUnitario) {
        this.precoUnitario = precoUnitario;
    }

    public BigDecimal getSubTotal() {
            return this.precoUnitario.multiply(new BigDecimal(this.quantidade));
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ItemVenda itemVenda = (ItemVenda) o;
        return Objects.equals(id, itemVenda.id);
    }

    @Override
    public int hashCode() {
        return ItemVenda.class.hashCode();
    }
}
