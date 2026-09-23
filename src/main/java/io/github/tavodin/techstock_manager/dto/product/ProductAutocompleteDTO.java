package io.github.tavodin.techstock_manager.dto.product;

import java.math.BigDecimal;

public record ProductAutocompleteDTO(
        Long id,
        String name,
        BigDecimal salePrice,
        Integer quantityInStock
) {
}
