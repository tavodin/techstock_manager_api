package io.github.tavodin.techstock_manager.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record CategorySpecificationRequestDTO(
        @NotNull(message = "Category ID is required!")
        Long categoryId,

        @NotNull(message = "Specification ID is required!")
        Long specificationId,

        @NotNull(message = "Required field is required!")
        Boolean required
) {
}
