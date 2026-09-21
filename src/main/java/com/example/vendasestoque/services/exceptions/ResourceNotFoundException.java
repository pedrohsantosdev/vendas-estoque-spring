package com.example.vendasestoque.services.exceptions;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(Object id) {
        super("Recurso não encontrado" + id);
    }
}
