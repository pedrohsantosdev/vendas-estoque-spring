package com.example.vendasestoque.entities.enuns;

public enum TipoMovimentacao {
    ENTRADA(1),
    SAIDA(2);

    private int code;

    private TipoMovimentacao(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static TipoMovimentacao valueOf(int code) {
        for(TipoMovimentacao tipo : TipoMovimentacao.values()) {

            if(tipo.code == code) {
                return tipo;
            }

        }

        throw new IllegalArgumentException("Código inválido!");
    }
}
