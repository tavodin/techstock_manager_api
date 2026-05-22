package io.github.tavodin.techstock_manager.assemblers;

import io.github.tavodin.techstock_manager.controllers.CategoryController;
import io.github.tavodin.techstock_manager.dto.CategorySpecificationDTO;
import io.github.tavodin.techstock_manager.entities.CategorySpecification;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class CategorySpecificationAssembler implements RepresentationModelAssembler<CategorySpecification, CategorySpecificationDTO> {

    @Override
    public CategorySpecificationDTO toModel(CategorySpecification entity) {
        CategorySpecificationDTO model = new CategorySpecificationDTO(entity);

        model.add(linkTo(methodOn(CategoryController.class)
                .save(null))
                .withRel("save")
                .withType("POST"));

        model.add(linkTo(methodOn(CategoryController.class)
                .updateCatSpec(model.getId(), null))
                .withRel("update")
                .withType("PUT"));

        model.add(linkTo(methodOn(CategoryController.class)
                .deleteCatSpec(model.getId()))
                .withRel("delete")
                .withType("DELETE"));

        return model;
    }
}
