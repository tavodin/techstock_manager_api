package io.github.tavodin.techstock_manager.dto.product;

import io.github.tavodin.techstock_manager.entities.Category;
import io.github.tavodin.techstock_manager.entities.CategorySpecification;
import io.github.tavodin.techstock_manager.entities.ProductSpecification;
import io.github.tavodin.techstock_manager.enums.SpecificationType;

import java.util.Set;

public class SpecificationLoadDTO {

    private Long specificationId;
    private String specificationName;
    private SpecificationType dataType;
    private String valueString;
    private Double valueNumber;
    private Boolean valueBoolean;
    private Boolean required;
    private String unitSymbol;

    public SpecificationLoadDTO() {
    }

    public SpecificationLoadDTO(
            ProductSpecification entity,
            Set<Category> categories) {

        Long specificationId = entity.getSpecification().getId();

        this.specificationId = specificationId;
        this.specificationName = entity.getSpecification().getName();
        this.dataType = entity.getSpecification().getDataType();
        this.valueString = entity.getValueString();
        this.valueNumber = entity.getValueNumber();
        this.valueBoolean = entity.getValueBoolean();

        this.required = categories.stream()
                .flatMap(category -> category.getCategorySpecifications().stream())
                .filter(categorySpecification ->
                        categorySpecification.getSpecification().getId().equals(specificationId))
                .anyMatch(CategorySpecification::getRequired);

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

    public Boolean getRequired() {
        return required;
    }

    public String getUnitSymbol() {
        return unitSymbol;
    }
}
