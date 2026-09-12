package io.github.tavodin.techstock_manager.dto.product;

import io.github.tavodin.techstock_manager.entities.Category;

public class CategoryLoadDTO {
    private Long id;
    private String name;

    public CategoryLoadDTO() {
    }

    public CategoryLoadDTO(Category entity) {
        this.id = entity.getId();
        this.name = entity.getName();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
