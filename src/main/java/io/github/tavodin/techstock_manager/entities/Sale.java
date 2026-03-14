package io.github.tavodin.techstock_manager.entities;

import io.github.tavodin.techstock_manager.enums.SaleStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class Sale extends AuditableEntity {

    @Column(name = "saleDate", nullable = false)
    private LocalDateTime saleDate;

    @Column(nullable = false)
    private SaleStatus status;

    @Column(name = "payment_method", length = 45, nullable = false)
    private String paymentMethod;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;
}
