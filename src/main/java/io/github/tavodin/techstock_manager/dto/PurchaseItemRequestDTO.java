package io.github.tavodin.techstock_manager.dto;

import java.math.BigDecimal;

public record PurchaseItemRequestDTO (
        Long productId,
        Integer quantity,
        BigDecimal unitCost
) {
}
