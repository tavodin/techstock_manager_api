package io.github.tavodin.techstock_manager.assemblers;

import io.github.tavodin.techstock_manager.controllers.SpecificationController;
import io.github.tavodin.techstock_manager.dto.SpecificationDTO;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class SpecificationAssembler implements RepresentationModelAssembler<SpecificationDTO, SpecificationDTO> {

    @Override
    public SpecificationDTO toModel(SpecificationDTO dto) {

        dto.add(linkTo(methodOn(SpecificationController.class)
                .findById(dto.getId()))
                .withSelfRel()
                .withType("GET"));

        dto.add(linkTo(methodOn(SpecificationController.class)
                .findAll(null))
                .withRel("findAll")
                .withType("GET"));

        dto.add(linkTo(methodOn(SpecificationController.class)
                .save(null))
                .withRel("save")
                .withType("POST"));

        dto.add(linkTo(methodOn(SpecificationController.class)
                .update(dto.getId(), null))
                .withRel("update")
                .withType("PUT"));

        dto.add(linkTo(methodOn(SpecificationController.class)
                .delete(dto.getId()))
                .withRel("delete")
                .withType("DELETE"));

        return dto;
    }
}
