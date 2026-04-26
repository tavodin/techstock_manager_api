package io.github.tavodin.techstock_manager.assemblers;

import io.github.tavodin.techstock_manager.controllers.UnitController;
import io.github.tavodin.techstock_manager.dto.UnitDTO;
import io.github.tavodin.techstock_manager.dto.UnitRequestDTO;
import io.github.tavodin.techstock_manager.entities.Unit;
import io.github.tavodin.techstock_manager.mappers.UnitMapper;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class UnitAssembler implements RepresentationModelAssembler<Unit, UnitDTO> {

    private final UnitMapper unitMapper;

    public UnitAssembler(UnitMapper unitMapper) {
        this.unitMapper = unitMapper;
    }

    @Override
    public UnitDTO toModel(Unit entity) {

        UnitDTO model = unitMapper.toDto(entity);

        model.add(linkTo(methodOn(UnitController.class)
                .findById(entity.getId()))
                .withSelfRel()
                .withType("GET"));

        model.add(linkTo(methodOn(UnitController.class)
                .findAll(null))
                .withRel("findAll")
                .withType("GET"));

        model.add(linkTo(methodOn(UnitController.class)
                .save(new UnitRequestDTO(entity.getName(), entity.getSymbol())))
                .withRel("save")
                .withType("POST"));

        model.add(linkTo(methodOn(UnitController.class)
                .update(entity.getId(), new UnitRequestDTO(entity.getName(), entity.getSymbol())))
                .withRel("update")
                .withType("PUT"));

        model.add(linkTo(methodOn(UnitController.class)
                .delete(entity.getId()))
                .withRel("delete")
                .withType("DELETE"));

        return model;
    }
}
