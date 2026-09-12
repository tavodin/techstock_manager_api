package io.github.tavodin.techstock_manager.dto.product;

import io.github.tavodin.techstock_manager.entities.Brand;

public class BrandLoadDTO {

    private Long id;
    private String name;

    public BrandLoadDTO() {
    }

    public BrandLoadDTO(Brand entity) {
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
