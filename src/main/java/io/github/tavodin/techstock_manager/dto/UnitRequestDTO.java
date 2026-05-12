package io.github.tavodin.techstock_manager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UnitRequestDTO(

        @Size(max = 45, message = "The name must contain a maximum of 45 characters.")
        String name,

        @NotBlank(message = "Unit Symbol is required!")
        @Size(max = 10, message = "The name must contain a maximum of 10 characters.")
        String symbol
) {
}
