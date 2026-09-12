package io.github.tavodin.techstock_manager.dto.product;

import io.github.tavodin.techstock_manager.entities.CategorySpecification;

public class CategorySpecificationLoadDTO {

    private Long categoryId;
    private Long specificationId;
    private Boolean required;

    public CategorySpecificationLoadDTO() {
    }

    public CategorySpecificationLoadDTO(CategorySpecification entity) {
        this.categoryId = entity.getCategory().getId();
        this.specificationId = entity.getSpecification().getId();
        this.required = entity.getRequired();
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public Long getSpecificationId() {
        return specificationId;
    }

    public Boolean getRequired() {
        return required;
    }
}
