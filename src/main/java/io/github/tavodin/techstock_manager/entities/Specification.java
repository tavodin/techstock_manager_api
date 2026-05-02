package io.github.tavodin.techstock_manager.entities;

import io.github.tavodin.techstock_manager.enums.SpecificationType;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Specification extends BaseEntity {

    @Column(length = 100, nullable = false)
    private String name;

    @Column(name = "data_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private SpecificationType dataType;

    @Column(nullable = false)
    private Boolean filterable;

    @ManyToOne
    @JoinColumn(name = "unit_id")
    private Unit unit;

    public Specification() {
    }

    public Specification(String name, SpecificationType type, Boolean filterable, Unit unit) {
        this.name = name;
        this.dataType = type;
        this.filterable = filterable;
        this.unit = unit;
    }

    public Specification(Long id, String name, SpecificationType type, Boolean filterable, Unit unit, LocalDateTime createdAt, LocalDateTime updatedAt) {
        super(id, createdAt, updatedAt);
        this.name = name;
        this.dataType = type;
        this.filterable = filterable;
        this.unit = unit;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SpecificationType getDataType() {
        return dataType;
    }

    public void setDataType(SpecificationType dataType) {
        this.dataType = dataType;
    }

    public Boolean getFilterable() {
        return filterable;
    }

    public void setFilterable(Boolean filterable) {
        this.filterable = filterable;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }
}
