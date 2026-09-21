package com.example.vendasestoque.resources.exceptions;

import com.example.vendasestoque.services.exceptions.EstoqueInsuficiente;
import com.example.vendasestoque.services.exceptions.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;

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
}
