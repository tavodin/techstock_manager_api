package io.github.tavodin.techstock_manager.dto;

import io.github.tavodin.techstock_manager.entities.ProductSpecification;

public class ProductSpecificationDTO {

    private Long id;
    private String valueString;
    private Double valueNumber;
    private Boolean valueBoolean;

    public ProductSpecificationDTO() {
    }

    public ProductSpecificationDTO(Long id, String valueString, Double valueNumber, Boolean valueBoolean) {
        this.id = id;
        this.valueString = valueString;
        this.valueNumber = valueNumber;
        this.valueBoolean = valueBoolean;
    }

    public ProductSpecificationDTO(ProductSpecification entity) {
        this.id = entity.getId();
        this.valueString = entity.getValueString();
        this.valueNumber = entity.getValueNumber();
        this.valueBoolean = entity.getValueBoolean();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getValueString() {
        return valueString;
    }

    public void setValueString(String valueString) {
        this.valueString = valueString;
    }

    public Double getValueNumber() {
        return valueNumber;
    }

    public void setValueNumber(Double valueNumber) {
        this.valueNumber = valueNumber;
    }

    public Boolean getValueBoolean() {
        return valueBoolean;
    }

    public void setValueBoolean(Boolean valueBoolean) {
        this.valueBoolean = valueBoolean;
    }
}
