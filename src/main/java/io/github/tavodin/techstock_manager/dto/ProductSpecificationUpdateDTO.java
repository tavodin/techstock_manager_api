package io.github.tavodin.techstock_manager.dto;

import org.hibernate.validator.constraints.Length;

public record ProductSpecificationUpdateDTO(
        @Length(max = 45, message = "Value must contain a maximum of {max} characters")
        String valueString,
        Double valueNumber,
        Boolean valueBoolean
) {
}
