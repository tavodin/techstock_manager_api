package io.github.tavodin.techstock_manager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UnitResponseDTO(

        @Size(max = 45, message = "The unit name must contain between 1 and 45 characters.")
        String name,

        @NotBlank(message = "Unit Symbol is required!")
        @Size(max = 10, message = "The Unit Symbol must contain between 1 and 10 characters.")
        String symbol
) {
}
