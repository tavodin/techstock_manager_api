package io.github.tavodin.techstock_manager.assemblers;

import io.github.tavodin.techstock_manager.controllers.PurchaseController;
import io.github.tavodin.techstock_manager.dto.purchase.PurchaseDTO;
import io.github.tavodin.techstock_manager.entities.Purchase;
import org.springframework.data.domain.PageRequest;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class PurchaseAssembler implements RepresentationModelAssembler<Purchase, PurchaseDTO> {

    public PurchaseDTO toModel(Purchase entity) {
        PurchaseDTO model = new PurchaseDTO();
        model.setId(entity.getId());
        model.setStatus(entity.getStatus());
        model.setPurchaseDate(entity.getPurchaseDate());
        model.setTotalAmount(entity.getTotalAmount());

        model.add(linkTo(methodOn(PurchaseController.class)
                .findById(model.getId()))
                .withSelfRel()
                .withType("GET"));

        model.add(linkTo(methodOn(PurchaseController.class)
                .findAll(null, null, null, null, null, PageRequest.of(0, 10)))
                .withRel("findAll")
                .withType("GET"));

        model.add(linkTo(methodOn(PurchaseController.class)
                .save(null))
                .withRel("save")
                .withType("POST"));

        model.add(linkTo(methodOn(PurchaseController.class)
                .completedPurchase(model.getId()))
                .withRel("completed")
                .withType("PATCH"));

        model.add(linkTo(methodOn(PurchaseController.class)
                .canceledPurchase(model.getId()))
                .withRel("canceled")
                .withType("PATCH"));

        return model;
    }
}
