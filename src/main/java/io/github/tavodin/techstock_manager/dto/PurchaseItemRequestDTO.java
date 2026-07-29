package io.github.tavodin.techstock_manager.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PurchaseItemRequestDTO (

        @NotNull(message = "Product is required")
        Long productId,

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be positive")
        Integer quantity,

        @NotNull(message = "Unit Cost is required")
        @Positive(message = "Unit Cost must be unit cost")
        BigDecimal unitCost
) {
}
