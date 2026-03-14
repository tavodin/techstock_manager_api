package io.github.tavodin.techstock_manager.entities;

import io.github.tavodin.techstock_manager.enums.SpecificationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Entity
public class Specification extends BaseEntity {

    @Column(length = 45, nullable = false)
    private String name;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SpecificationType type;

    @Column(nullable = false)
    private Boolean filterable;
}
