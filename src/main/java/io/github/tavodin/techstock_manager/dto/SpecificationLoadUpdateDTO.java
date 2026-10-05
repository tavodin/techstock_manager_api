package io.github.tavodin.techstock_manager.dto;

import io.github.tavodin.techstock_manager.enums.SpecificationType;

public class SpecificationLoadUpdateDTO {
    private String name;
    private SpecificationType dataType;
    private Boolean filterable;
    private UnitAutocompleteDTO unit;

    public SpecificationLoadUpdateDTO() {
    }

    public SpecificationLoadUpdateDTO(String name, SpecificationType dataType, Boolean filterable, Long unitId, String unitName) {
        this.name = name;
        this.dataType = dataType;
        this.filterable = filterable;

        if(unitId != null) {
            this.unit = new UnitAutocompleteDTO(unitId, unitName);
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
