package io.github.tavodin.techstock_manager.dto;

import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

public record ProductSpecificationRequestDTO(
        @NotNull(message = "Specification ID is required")
        Long specificationId,

        @Length(max = 45, message = "Value must contain a maximum of {max} characters")
        String valueString,

        Double valueNumber,
        Boolean valueBoolean
) {
}
