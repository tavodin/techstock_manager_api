package io.github.tavodin.techstock_manager.dto;

public class CategorySpecificationsListDTO {

    private Long id;
    private String specificationName;
    private Boolean isRequired;

    public CategorySpecificationsListDTO() {
    }

    public CategorySpecificationsListDTO(Long id, String specificationName, Boolean isRequired) {
        this.id = id;
        this.specificationName = specificationName;
        this.isRequired = isRequired;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
