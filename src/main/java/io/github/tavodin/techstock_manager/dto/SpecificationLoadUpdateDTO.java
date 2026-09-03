package io.github.tavodin.techstock_manager.dto;

import io.github.tavodin.techstock_manager.entities.Specification;
import io.github.tavodin.techstock_manager.enums.SpecificationType;

public class SpecificationLoadUpdateDTO {
    private String name;
    private SpecificationType dataType;
    private Boolean filterable;
    private UnitAutocompleteDTO unit;

    public SpecificationLoadUpdateDTO() {
    }

    public SpecificationLoadUpdateDTO(Specification specification) {
        this.name = specification.getName();
        this.dataType = specification.getDataType();
        this.filterable = specification.getFilterable();

        if(specification.getUnit() != null) {
            this.unit = new UnitAutocompleteDTO(
                    specification.getUnit().getId(),
                    specification.getUnit().getName()
            );
        }
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

    public UnitAutocompleteDTO getUnit() {
        return unit;
    }

    public void setUnit(UnitAutocompleteDTO unit) {
        this.unit = unit;
    }
}
