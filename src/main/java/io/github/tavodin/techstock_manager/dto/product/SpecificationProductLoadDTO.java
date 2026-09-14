package io.github.tavodin.techstock_manager.dto.product;

import io.github.tavodin.techstock_manager.entities.ProductSpecification;
import io.github.tavodin.techstock_manager.enums.SpecificationType;

public class SpecificationProductLoadDTO {

    private Long specificationId;
    private String specificationName;
    private SpecificationType dataType;
    private String valueString;
    private Double valueNumber;
    private Boolean valueBoolean;
    private String unitSymbol;

    public SpecificationProductLoadDTO() {
    }

    public SpecificationProductLoadDTO(ProductSpecification entity) {

        Long specificationId = entity.getSpecification().getId();

        this.specificationId = specificationId;
        this.specificationName = entity.getSpecification().getName();
        this.dataType = entity.getSpecification().getDataType();
        this.valueString = entity.getValueString();
        this.valueNumber = entity.getValueNumber();
        this.valueBoolean = entity.getValueBoolean();

        if (entity.getSpecification().getUnit() != null) {
            this.unitSymbol = entity.getSpecification().getUnit().getSymbol();
        }
    }

    public Long getSpecificationId() {
        return specificationId;
    }

    public String getSpecificationName() {
        return specificationName;
    }

    public SpecificationType getDataType() {
        return dataType;
    }

    public String getValueString() {
        return valueString;
    }

    public Double getValueNumber() {
        return valueNumber;
    }

    public Boolean getValueBoolean() {
        return valueBoolean;
    }

    public String getUnitSymbol() {
        return unitSymbol;
    }
}
