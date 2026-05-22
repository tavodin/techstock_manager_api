package io.github.tavodin.techstock_manager.dto;

import io.github.tavodin.techstock_manager.entities.Brand;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

@Relation(itemRelation = "brand", collectionRelation = "brands")
public class BrandDTO extends RepresentationModel<BrandDTO> {
    private Long id;
    private String name;

    public BrandDTO() {
    }

    public BrandDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public BrandDTO(Brand entity) {
        this.id = entity.getId();
        this.name = entity.getName();
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
