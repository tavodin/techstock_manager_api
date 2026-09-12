package io.github.tavodin.techstock_manager.dto;

import io.github.tavodin.techstock_manager.enums.SpecificationType;

public record CategorySpecificationsInputDTO(
        Long categoryId,
        Long specificationId,
        String specificationName,
        SpecificationType specificationType,
        Boolean required,
        String unitSymbol
){
}
