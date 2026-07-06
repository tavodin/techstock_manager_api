package io.github.tavodin.techstock_manager.assemblers;

import io.github.tavodin.techstock_manager.controllers.CategoryController;
import io.github.tavodin.techstock_manager.dto.CategoryDTO;
import io.github.tavodin.techstock_manager.entities.Category;
import org.springframework.data.domain.PageRequest;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class CategoryAssembler implements RepresentationModelAssembler<Category, CategoryDTO> {

    @Override
    public CategoryDTO toModel(Category entity) {
        CategoryDTO model = new CategoryDTO(entity);

        model.add(linkTo(methodOn(CategoryController.class)
                .findById(model.getId()))
                .withSelfRel()
                .withType("GET"));

        model.add(linkTo(methodOn(CategoryController.class)
                .findAll(PageRequest.of(0, 10)))
                .withRel("findAll")
                .withType("GET"));

        model.add(linkTo(methodOn(CategoryController.class)
                .save(null))
                .withRel("save")
                .withType("POST"));

        model.add(linkTo(methodOn(CategoryController.class)
                .update(model.getId(), null))
                .withRel("updateProduct")
                .withType("PUT"));

        model.add(linkTo(methodOn(CategoryController.class)
                .delete(model.getId()))
                .withRel("delete")
                .withType("DELETE"));

        return model;
    }
}
