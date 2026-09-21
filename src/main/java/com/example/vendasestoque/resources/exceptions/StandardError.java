package com.example.vendasestoque.resources.exceptions;

import java.time.Instant;

public class StandardError {

    private Instant momento;
    private Integer status;
    private String error;
    private String mensagem;
    private String caminho;

    public StandardError() {
    }

    public StandardError(Instant momento, Integer status, String error, String mensagem, String caminho) {
        this.momento = momento;
        this.status = status;
        this.error = error;
        this.mensagem = mensagem;
        this.caminho = caminho;
    }

    public Instant getMomento() {
        return momento;
    }

    public void setMomento(Instant momento) {
        this.momento = momento;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public String getCaminho() {
        return caminho;
    }

    public void setCaminho(String caminho) {
        this.caminho = caminho;
    }
}
