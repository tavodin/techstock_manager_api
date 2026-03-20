package io.github.tavodin.techstock_manager.controllers.handlers;

import io.github.tavodin.techstock_manager.dto.error.CustomError;
import io.github.tavodin.techstock_manager.dto.error.FieldError;
import io.github.tavodin.techstock_manager.dto.error.ValidationError;
import io.github.tavodin.techstock_manager.exceptions.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class ExceptionHandlerController {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<CustomError> resourceNotFoundHandler(ResourceNotFoundException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;

        CustomError error = new CustomError(
                Instant.now(),
                status.value(),
                ex.getMessage(),
                request.getRequestURI());

        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationError> businessHandler(MethodArgumentNotValidException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;

        List<FieldError> errors = ex.getFieldErrors()
                .stream()
                .map(error -> new FieldError(error.getField(), error.getDefaultMessage()))
                .toList();

        ValidationError error = new ValidationError(
                Instant.now(),
                status.value(),
                "Entity validation error",
                request.getRequestURI(),
                errors);

        return ResponseEntity.status(status).body(error);
    }
}
