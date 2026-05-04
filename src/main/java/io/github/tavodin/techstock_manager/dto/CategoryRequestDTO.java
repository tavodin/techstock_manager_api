package io.github.tavodin.techstock_manager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequestDTO(

        @NotBlank(message = "Name is required!")
        @Size(max = 100, message = "Name must contain 100 characters.")
        String name
) {
}
