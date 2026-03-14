package io.github.tavodin.techstock_manager.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "product_specification")
public class ProductSpecification extends BaseEntity {

    @Column(name = "value_string",length = 45, nullable = false)
    private String valueString;

    @Column(name = "value_number", nullable = false)
    private Double valueNumber;

    @Column(name = "value_boolean", nullable = false)
    private Boolean valueBoolean;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "specification_id")
    private Specification specification;
}
