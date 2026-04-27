package io.github.tavodin.techstock_manager.assemblers;

import io.github.tavodin.techstock_manager.controllers.SpecificationController;
import io.github.tavodin.techstock_manager.dto.SpecificationDTO;
import io.github.tavodin.techstock_manager.entities.Specification;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class SpecificationAssembler implements RepresentationModelAssembler<Specification, SpecificationDTO> {

    @Override
    public SpecificationDTO toModel(Specification entity) {

        SpecificationDTO model = new SpecificationDTO();

        model.setId(entity.getId());
        model.setName(entity.getName());
        model.setDataType(entity.getDataType().name());
        model.setFilterable(entity.getFilterable());
        model.setCreatedAt(entity.getCreatedAt());
        model.setUpdatedAt(entity.getUpdatedAt());

        model.add(linkTo(methodOn(SpecificationController.class)
                .findById(model.getId()))
                .withSelfRel()
                .withType("GET"));

        model.add(linkTo(methodOn(SpecificationController.class)
                .findAll(null))
                .withRel("findAll")
                .withType("GET"));

        model.add(linkTo(methodOn(SpecificationController.class)
                .save(null))
                .withRel("save")
                .withType("POST"));

        model.add(linkTo(methodOn(SpecificationController.class)
                .update(model.getId(), null))
                .withRel("update")
                .withType("PUT"));

        model.add(linkTo(methodOn(SpecificationController.class)
                .delete(model.getId()))
                .withRel("delete")
                .withType("DELETE"));

        return model;
    }
}
