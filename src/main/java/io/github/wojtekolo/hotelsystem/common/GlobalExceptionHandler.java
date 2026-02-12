package io.github.wojtekolo.hotelsystem.common;

import io.github.wojtekolo.hotelsystem.common.exceptions.ResourceAlreadyExistsException;
import io.github.wojtekolo.hotelsystem.common.exceptions.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tools.jackson.databind.exc.InvalidFormatException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> result = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> result.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotExistingResource(ResourceNotFoundException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleJsonErrors(HttpMessageNotReadableException ex){
        Map<String, String> result = new HashMap<>();

        Throwable cause = ex.getCause();

        if(cause instanceof InvalidFormatException ifex){
            result.put(ifex.getPath().get(0).getPropertyName(),"Invalid input: "+ifex.getValue()+", expected type: "+ifex.getTargetType().getSimpleName());
        } else {
            result.put("error", "json error");
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleExistingResource(ResourceAlreadyExistsException ex){
        Map<String, String> result = new HashMap<>();
        result.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(result);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String,String>> handleIllegalState(IllegalStateException ex){
        Map<String, String> result = new HashMap<>();
        result.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
    }
}