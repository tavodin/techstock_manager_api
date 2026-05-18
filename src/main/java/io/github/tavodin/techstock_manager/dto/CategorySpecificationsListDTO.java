package io.github.tavodin.techstock_manager.dto;

public class CategorySpecificationsListDTO {

    private Long categorySpecificationId;
    private String specificationName;
    private Boolean isRequired;

    public CategorySpecificationsListDTO() {
    }

    public CategorySpecificationsListDTO(Long categorySpecificationId, String specificationName, Boolean isRequired) {
        this.categorySpecificationId = categorySpecificationId;
        this.specificationName = specificationName;
        this.isRequired = isRequired;
    }

    public Long getCategorySpecificationId() {
        return categorySpecificationId;
    }

    public void setCategorySpecificationId(Long categorySpecificationId) {
        this.categorySpecificationId = categorySpecificationId;
    }

    public String getSpecificationName() {
        return specificationName;
    }

    public void setSpecificationName(String specificationName) {
        this.specificationName = specificationName;
    }

    public Boolean getIsRequired() {
        return isRequired;
    }

    public void setIsRequired(Boolean isRequired) {
        this.isRequired = isRequired;
    }
}
