package io.github.tavodin.techstock_manager.assemblers;

import io.github.tavodin.techstock_manager.controllers.StockMovementController;
import io.github.tavodin.techstock_manager.dto.StockMovementDTO;
import org.springframework.data.domain.PageRequest;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class StockMovementAssembler implements RepresentationModelAssembler<StockMovementDTO, StockMovementDTO> {

    @Override
    public StockMovementDTO toModel(StockMovementDTO dto) {
        dto.add(linkTo(methodOn(StockMovementController.class)
                .findAll(PageRequest.of(0, 10)))
                .withRel("findAll")
                .withType("GET"));

        return dto;
    }
}
