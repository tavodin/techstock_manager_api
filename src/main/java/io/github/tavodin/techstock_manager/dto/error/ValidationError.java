package io.github.tavodin.techstock_manager.dto.error;

import java.time.Instant;
import java.util.List;

public class ValidationError extends CustomError {

    private List<FieldError> errors;

    public ValidationError(Instant timestamp, int status, String message, String path, List<FieldError> errors) {
        super(timestamp, status, message, path);
        this.errors = errors;
    }

    public List<FieldError> getErrors() {
        return errors;
    }

    public void setErrors(List<FieldError> errors) {
        this.errors = errors;
    }
}
