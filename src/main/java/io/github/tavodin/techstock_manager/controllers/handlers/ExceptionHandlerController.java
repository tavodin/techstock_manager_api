package io.github.tavodin.techstock_manager.controllers.handlers;

import io.github.tavodin.techstock_manager.dto.error.CustomError;
import io.github.tavodin.techstock_manager.dto.error.FieldError;
import io.github.tavodin.techstock_manager.dto.error.ValidationError;
import io.github.tavodin.techstock_manager.exceptions.AlreadyExistsException;
import io.github.tavodin.techstock_manager.exceptions.BusinessException;
import io.github.tavodin.techstock_manager.exceptions.EntityInUseException;
import io.github.tavodin.techstock_manager.exceptions.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

    @ExceptionHandler(AlreadyExistsException.class)
    public ResponseEntity<CustomError> alreadyExistsHandler(AlreadyExistsException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;

        CustomError error = new CustomError(
                Instant.now(),
                status.value(),
                ex.getMessage(),
                request.getRequestURI());

        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<CustomError> businessHandler(BusinessException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        CustomError error = new CustomError(
                Instant.now(),
                status.value(),
                ex.getMessage(),
                request.getRequestURI());

        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(EntityInUseException.class)
    public ResponseEntity<CustomError> entityInUseHandler(EntityInUseException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;

        CustomError error = new CustomError(
                Instant.now(),
                status.value(),
                ex.getMessage(),
                request.getRequestURI());

        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<CustomError> httpMessageNotReadableException(HttpMessageNotReadableException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        Throwable cause = ex.getCause();

        List<FieldError> errors = new ArrayList<>();

        if(cause instanceof com.fasterxml.jackson.databind.exc.InvalidFormatException invalidEx) {
            String fieldName = invalidEx.getPath().get(0).getFieldName();

            Class<?> targetType = invalidEx.getTargetType();

            if(targetType.isEnum()) {
                Object[] enumValues = targetType.getEnumConstants();

                String allowedValues = Arrays.stream(enumValues)
                        .map(Object::toString)
                        .collect(Collectors.joining(", "));

                errors.add(new FieldError(
                        fieldName,
                        "Invalid value. Allowed values: " + allowedValues
                ));
            }
        }

        ValidationError error = new ValidationError(Instant.now(),
                status.value(),
                "Entity validation error",
                request.getRequestURI(),
                errors);

        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationError> validationErrorHandler(MethodArgumentNotValidException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

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
