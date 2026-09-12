package io.github.tavodin.techstock_manager.dto;

import io.github.tavodin.techstock_manager.enums.SpecificationType;

public class CategorySpecificationsListDTO {
    private Long catSpecId;
    private String specificationName;
    private SpecificationType dataType;
    private Boolean isRequired;

    public CategorySpecificationsListDTO() {
    }

    public CategorySpecificationsListDTO(Long catSpecId, String specificationName, SpecificationType dataType, Boolean isRequired) {
        this.catSpecId = catSpecId;
        this.specificationName = specificationName;
        this.dataType = dataType;
        this.isRequired = isRequired;
    }

    public Long getCatSpecId() {
        return catSpecId;
    }

    public void setCatSpecId(Long catSpecId) {
        this.catSpecId = catSpecId;
    }

    public String getSpecificationName() {
        return specificationName;
    }

    public void setSpecificationName(String specificationName) {
        this.specificationName = specificationName;
    }

    public SpecificationType getDataType() {
        return dataType;
    }

    public void setDataType(SpecificationType dataType) {
        this.dataType = dataType;
    }

    public Boolean getRequired() {
        return isRequired;
    }

    public void setRequired(Boolean required) {
        isRequired = required;
    }
}
