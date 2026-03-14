package io.github.tavodin.techstock_manager.entities;

import io.github.tavodin.techstock_manager.enums.PurchaseStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class Purchase extends AuditableEntity {

    @Column(name = "purchase_date", nullable = false)
    private LocalDateTime purchaseDate;

    @Column(nullable = false)
    private PurchaseStatus status;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;
}
