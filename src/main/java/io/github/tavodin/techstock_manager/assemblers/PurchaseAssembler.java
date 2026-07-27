package io.github.tavodin.techstock_manager.assemblers;

import io.github.tavodin.techstock_manager.dto.PurchaseDTO;
import io.github.tavodin.techstock_manager.entities.Purchase;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class PurchaseAssembler implements RepresentationModelAssembler<Purchase, PurchaseDTO> {

    @Override
    public PurchaseDTO toModel(Purchase entity) {
        PurchaseDTO model = new PurchaseDTO();
        model.setId(entity.getId());
        model.setStatus(entity.getStatus());
        model.setPurchaseDate(entity.getPurchaseDate());
        model.setTotalAmount(entity.getTotalAmount());

        return model;
    }
}
