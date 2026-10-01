package com.example.vendasestoque.resources.exceptions;

import com.example.vendasestoque.services.exceptions.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.stream.Collectors;

@ControllerAdvice
public class ResourceExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<StandardError> resourceNotFound(ResourceNotFoundException e, HttpServletRequest request) {

        String error = "Recurso não encontrado";
        HttpStatus status = HttpStatus.NOT_FOUND;

        StandardError err = new StandardError(
                Instant.now(), status.value(), error, e.getMessage(), request.getRequestURI()
        );

        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(EstoqueInsuficiente.class)
    public ResponseEntity<StandardError> estoqueInsuficiente(EstoqueInsuficiente e, HttpServletRequest request) {

        String error = "Estoque insuficiente";
        HttpStatus status = HttpStatus.CONFLICT;

        StandardError err = new StandardError(
                Instant.now(), status.value(), error, e.getMessage(), request.getRequestURI()
        );

        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<StandardError> entradaInvalida(MethodArgumentNotValidException e, HttpServletRequest request) {

        String error = "Entrada inválida";
        HttpStatus status = HttpStatus.BAD_REQUEST;

        String mensagem = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(erro -> erro.getField() + ": " + erro.getDefaultMessage())
                .collect(Collectors.joining("; "));

        StandardError err = new StandardError(
                Instant.now(), status.value(), error, mensagem, request.getRequestURI()
        );

        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(ItemRepetidoNaCompra.class)
    public ResponseEntity<StandardError> itemRepetidoNaCompra(ItemRepetidoNaCompra e, HttpServletRequest request) {

        String error = "Produtos inválidos";
        HttpStatus status = HttpStatus.BAD_REQUEST;

        StandardError err = new StandardError(
                Instant.now(), status.value(), error, e.getMessage(), request.getRequestURI()
        );

        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(CodigoExistente.class)
    public ResponseEntity<StandardError> codigoExistente(CodigoExistente e, HttpServletRequest request) {

        String error = "Código inválido";
        HttpStatus status = HttpStatus.CONFLICT;

        StandardError err = new StandardError(
                Instant.now(), status.value(), error, e.getMessage(), request.getRequestURI()
        );

        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<StandardError> integridadeViolada(DataIntegrityViolationException e, HttpServletRequest request) {

        String error = "Item associado a outro, impossível deletar";
        HttpStatus status = HttpStatus.CONFLICT;

        StandardError err = new StandardError(
                Instant.now(), status.value(), error, "Conflito no banco, não é possível deletar um item que possui associações",
                request.getRequestURI()
        );

        return ResponseEntity.status(status).body(err);
    }
}
