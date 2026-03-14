package io.github.tavodin.techstock_manager.entities;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "purchase_item")
public class PurchaseItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_cost", nullable = false)
    private BigDecimal unitCost;

    @Column(name = "subtotal", nullable = false)
    private BigDecimal subtotal;
}
