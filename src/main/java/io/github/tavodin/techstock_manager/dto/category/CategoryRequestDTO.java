package io.github.tavodin.techstock_manager.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record CategoryRequestDTO(

        @NotBlank(message = "Name is required!")
        @Size(max = 100, message = "The name must contain a maximum of 100 characters.")
        String name,

        Set<Long> specificationIds
) {
}
