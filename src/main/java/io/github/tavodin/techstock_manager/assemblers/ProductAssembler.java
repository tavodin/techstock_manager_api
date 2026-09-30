package io.github.tavodin.techstock_manager.assemblers;

import io.github.tavodin.techstock_manager.controllers.ProductController;
import io.github.tavodin.techstock_manager.dto.product.ProductDTO;
import io.github.tavodin.techstock_manager.entities.Product;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ProductAssembler implements RepresentationModelAssembler<Product, ProductDTO> {

    @Override
    public ProductDTO toModel(Product entity) {
        ProductDTO model = new ProductDTO(entity);

        model.add(linkTo(methodOn(ProductController.class)
                .findById(model.getId()))
                .withSelfRel());

        model.add(linkTo(methodOn(ProductController.class)
                .getAll(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                ))
                .withRel("findAll")
                .withType("GET"));

        model.add(linkTo(methodOn(ProductController.class)
                .save(null))
                .withRel("save")
                .withType("POST"));

        model.add(linkTo(methodOn(ProductController.class)
                .update(entity.getId(), null))
                .withRel("update")
                .withType("PUT"));

        model.add(linkTo(methodOn(ProductController.class)
                .delete(entity.getId()))
                .withRel("delete")
                .withType("DELETE"));


        return model;
    }
}
