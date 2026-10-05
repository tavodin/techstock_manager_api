package io.github.tavodin.techstock_manager.dto;

import io.github.tavodin.techstock_manager.entities.Specification;

public class CategorySpecificationsListDTO {
    private Long specificationId;
    private String specificationName;

    public CategorySpecificationsListDTO() {
    }

    public CategorySpecificationsListDTO(Specification specification) {
        this.specificationId = specification.getId();
        this.specificationName = specification.getName();
    }

    public CategorySpecificationsListDTO(Long specificationId, String specificationName) {
        this.specificationId = specificationId;
        this.specificationName = specificationName;
    }

    public Long getSpecificationId() {
        return specificationId;
    }

    public void setSpecificationId(Long specificationId) {
        this.specificationId = specificationId;
    }

    public String getSpecificationName() {
        return specificationName;
    }

    public void setSpecificationName(String specificationName) {
        this.specificationName = specificationName;
    }
}
