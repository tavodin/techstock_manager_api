package io.github.tavodin.techstock_manager.assemblers;

import io.github.tavodin.techstock_manager.dto.SaleDTO;
import io.github.tavodin.techstock_manager.entities.Sale;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class SaleAssembler implements RepresentationModelAssembler<Sale, SaleDTO> {

    @Override
    public SaleDTO toModel(Sale entity) {
        SaleDTO model = new SaleDTO();
        model.setId(entity.getId());
        model.setSaleDate(entity.getSaleDate());
        model.setPaymentMethod(entity.getPaymentMethod());
        model.setStatus(entity.getStatus());
        model.setTotalAmount(entity.getTotalAmount());

        return model;
    }
}
