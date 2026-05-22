package io.github.tavodin.techstock_manager.assemblers;

import io.github.tavodin.techstock_manager.controllers.BrandController;
import io.github.tavodin.techstock_manager.dto.BrandDTO;
import io.github.tavodin.techstock_manager.entities.Brand;
import org.springframework.data.domain.PageRequest;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class BrandAssembler implements RepresentationModelAssembler<Brand, BrandDTO> {

    @Override
    public BrandDTO toModel(Brand entity) {
        BrandDTO model = new BrandDTO(entity);

        model.add(linkTo(methodOn(BrandController.class)
                .findById(model.getId()))
                .withSelfRel()
                .withType("GET"));

        model.add(linkTo(methodOn(BrandController.class)
                .findAll(PageRequest.of(0, 10)))
                .withRel("findAll")
                .withType("GET"));

        model.add(linkTo(methodOn(BrandController.class)
                .save(null))
                .withRel("save")
                .withType("POST"));

        model.add(linkTo(methodOn(BrandController.class)
                .update(model.getId(), null))
                .withRel("update")
                .withType("UPDATE"));

        model.add(linkTo(methodOn(BrandController.class)
                .delete(model.getId()))
                .withRel("delete")
                .withType("DELETE"));

        return model;
    }
}
