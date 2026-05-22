package io.github.tavodin.techstock_manager.dto;

import io.github.tavodin.techstock_manager.entities.CategorySpecification;
import org.springframework.hateoas.RepresentationModel;

public class CategorySpecificationDTO extends RepresentationModel<CategorySpecificationDTO> {
    private Long id;
    private Long categoryId;
    private Long specificationId;
    private Boolean required;

    public CategorySpecificationDTO() {
    }

    public CategorySpecificationDTO(Long id, Long categoryId, Long specificationId, Boolean required) {
        this.id = id;
        this.categoryId = categoryId;
        this.specificationId = specificationId;
        this.required = required;
    }

    public CategorySpecificationDTO(CategorySpecification entity) {
        id = entity.getId();
        categoryId = entity.getCategory().getId();
        specificationId = entity.getSpecification().getId();
        required = entity.getRequired();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Long getSpecificationId() {
        return specificationId;
    }

    public void setSpecificationId(Long specificationId) {
        this.specificationId = specificationId;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }
}
