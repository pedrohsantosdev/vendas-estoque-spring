package com.example.vendasestoque.services.exceptions;

public class EstoqueInsuficiente extends RuntimeException {
    public EstoqueInsuficiente(String message) {
        super(message);
    }
}
