package io.github.tavodin.techstock_manager.dto.category;

import io.github.tavodin.techstock_manager.entities.Specification;

public class SpecificationCategoryLoadDTO {
    private Long id;
    private String name;

    public SpecificationCategoryLoadDTO() {
    }

    public SpecificationCategoryLoadDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public SpecificationCategoryLoadDTO(Specification specification) {
        this.id = specification.getId();
        this.name = specification.getName();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
