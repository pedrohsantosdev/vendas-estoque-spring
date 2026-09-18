package com.example.vendasestoque.entities.enuns;

public enum StatusVenda {

    CONFIRMADA(1),
    CANCELADA(2);

    private int code;

    private StatusVenda(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static StatusVenda valueOf(int code) {
        for(StatusVenda status : StatusVenda.values()) {

            if(status.code == code) {
                return status;
            }
        }

        throw  new IllegalArgumentException("Codigo inválido!");
    }
}
