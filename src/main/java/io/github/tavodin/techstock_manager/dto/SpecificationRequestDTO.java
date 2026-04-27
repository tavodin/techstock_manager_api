package io.github.tavodin.techstock_manager.dto;

import io.github.tavodin.techstock_manager.enums.SpecificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SpecificationRequestDTO(

        @NotBlank(message = "Name is required!")
        @Size(max = 100, message = "Name must contain 100 characters.")
        String name,

        @NotNull(message = "Data Type is required!")
        SpecificationType dataType,

        @NotNull(message = "Filterable is required!")
        Boolean filterable,

        @NotNull(message = "Unit ID is required!")
        Long unitId
) {
}
