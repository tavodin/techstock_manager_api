package io.github.tavodin.techstock_manager.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "product_specification")
public class ProductSpecification extends BaseEntity {

    @Column(name = "value_string",length = 45, nullable = false)
    private String valueString;

    @Column(name = "value_number", nullable = false)
    private Double valueNumber;

    @Column(name = "value_boolean", nullable = false)
    private Boolean valueBoolean;
}
