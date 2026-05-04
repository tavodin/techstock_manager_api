package io.github.tavodin.techstock_manager.builder;

import io.github.tavodin.techstock_manager.entities.Specification;
import io.github.tavodin.techstock_manager.entities.Unit;
import io.github.tavodin.techstock_manager.enums.SpecificationType;

import java.time.LocalDateTime;

public class SpecificationBuilder {

    private Long id = 1L;
    private String name = "RAM";
    private SpecificationType dataType = SpecificationType.NUMBER;
    private Boolean filterable = true;
    private LocalDateTime createdAt = LocalDateTime.of(2026, 3, 30, 10, 0);
    private LocalDateTime updatedAt = createdAt.plusHours(1L);
    private Unit unit = new Unit(1L, createdAt, updatedAt, "Gigabyte", "GB");

    public static SpecificationBuilder builder() {
        return new SpecificationBuilder();
    }

    public SpecificationBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public SpecificationBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public SpecificationBuilder withSpecificationType(SpecificationType dataType) {
        this.dataType = dataType;
        return this;
    }

    public SpecificationBuilder withFilterable(Boolean filterable) {
        this.filterable = filterable;
        return this;
    }

    public SpecificationBuilder withCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public SpecificationBuilder withUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    public SpecificationBuilder withUnit(Unit unit) {
        this.unit = unit;
        return this;
    }

    public Specification build() {
        return new Specification(id, name, dataType, filterable, unit, createdAt, updatedAt);
    }
}
