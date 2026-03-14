package io.github.tavodin.techstock_manager.entities;

import io.github.tavodin.techstock_manager.enums.SpecificationType;
import jakarta.persistence.*;

@Entity
public class Specification extends BaseEntity {

    @Column(length = 45, nullable = false)
    private String name;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SpecificationType type;

    @Column(nullable = false)
    private Boolean filterable;

    @ManyToOne
    @JoinColumn(name = "unit_id")
    private Unit unit;

    public Specification() {
    }

    public Specification(String name, SpecificationType type, Boolean filterable, Unit unit) {
        this.name = name;
        this.type = type;
        this.filterable = filterable;
        this.unit = unit;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SpecificationType getType() {
        return type;
    }

    public void setType(SpecificationType type) {
        this.type = type;
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
