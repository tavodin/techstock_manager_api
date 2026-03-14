package io.github.tavodin.techstock_manager.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import java.math.BigDecimal;

@Entity
public class Product extends AuditableEntity {

    @Column(length = 200, nullable = false)
    private String name;

    @Column(name = "cost_price", nullable = false)
    private BigDecimal costPrice;

    @Column(name = "sale_price", nullable = false)
    private BigDecimal salePrice;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(length = 10, nullable = false)
    private String varchar;

    @Column(name = "quantity_in_stock", nullable = false)
    private Integer quantityInStock;

    @Column(name = "minimum_stock", nullable = false)
    private Integer minimum_stock;

    @Column(nullable = false)
    private Boolean active;
}
