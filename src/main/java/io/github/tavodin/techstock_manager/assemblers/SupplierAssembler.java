package io.github.tavodin.techstock_manager.assemblers;

import io.github.tavodin.techstock_manager.dto.SupplierDTO;
import io.github.tavodin.techstock_manager.entities.Supplier;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class SupplierAssembler implements RepresentationModelAssembler<Supplier, SupplierDTO> {

    @Override
    public SupplierDTO toModel(Supplier entity) {
        SupplierDTO model = new SupplierDTO(entity);
        return model;
    }
}
