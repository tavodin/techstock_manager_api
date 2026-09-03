package io.github.tavodin.techstock_manager.assemblers;

import io.github.tavodin.techstock_manager.controllers.SupplierController;
import io.github.tavodin.techstock_manager.dto.SupplierDTO;
import io.github.tavodin.techstock_manager.entities.Supplier;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class SupplierAssembler implements RepresentationModelAssembler<Supplier, SupplierDTO> {

    @Override
    public SupplierDTO toModel(Supplier entity) {
        SupplierDTO model = new SupplierDTO(entity);

        model.add(linkTo(methodOn(SupplierController.class)
                .findById(entity.getId()))
                .withSelfRel()
                .withType("GET"));

        model.add(linkTo(methodOn(SupplierController.class)
                .findAll(null, null))
                .withRel("findAll")
                .withType("GET"));

        model.add(linkTo(methodOn(SupplierController.class)
                .save(null))
                .withRel("save")
                .withType("POST"));

        model.add(linkTo(methodOn(SupplierController.class)
                .update(entity.getId(), null))
                .withRel("update")
                .withType("PUT"));

        model.add(linkTo(methodOn(SupplierController.class)
                .delete(entity.getId()))
                .withRel("delete")
                .withType("DELETE"));

        return model;
    }
}
