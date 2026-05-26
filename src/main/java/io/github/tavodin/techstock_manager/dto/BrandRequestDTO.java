package io.github.tavodin.techstock_manager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BrandRequestDTO(
        @NotBlank(message = "Name is required")
        @Size(min  = 2, max = 100, message = "Name must contain between {min} and {max} characters")
        String name
) {
}
