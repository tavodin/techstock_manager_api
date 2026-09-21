package io.github.tavodin.techstock_manager.controllers;

import io.github.tavodin.techstock_manager.dto.product.ProductAutocompleteDTO;
import io.github.tavodin.techstock_manager.dto.product.*;
import io.github.tavodin.techstock_manager.services.ProductService;
import io.github.tavodin.techstock_manager.services.ProductSpecificationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService service;
    private final ProductSpecificationService prodSpecService;

    public ProductController(ProductService service, ProductSpecificationService prodSpecService) {
        this.service = service;
        this.prodSpecService = prodSpecService;
    }

    @PreAuthorize("hasAuthority('READ_PRODUCT')")
    @GetMapping("/{id}")
    public ProductDTO findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PreAuthorize("hasAuthority('READ_PRODUCT')")
    @GetMapping("/{id}/load")
    public ProductLoadDTO loadProductById(@PathVariable Long id) {
        return service.loadProductById(id);
    }

    @PreAuthorize("hasAuthority('READ_PRODUCT')")
    @GetMapping
    public PagedModel<ProductDTO> getAll(
            @RequestParam(value = "name", required = false)
            String name,
            Pageable pageable
    ) {
        return service.findAll(name, pageable);
    }

    @PreAuthorize("hasAuthority('READ_PRODUCT')")
    @GetMapping("/autocomplete")
    public List<ProductAutocompleteDTO> getAllAutocomplete(@RequestParam("name") String name) {
        return service.getAllAutocomplete(name);
    }

    @PreAuthorize("hasAuthority('CREATE_PRODUCT')")
    @PostMapping
    public ResponseEntity<ProductDTO> save(@RequestBody @Valid ProductRequestDTO request) {
        ProductDTO dto = service.save(request);

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(dto.getId())
                .toUri();

        return ResponseEntity.created(uri).body(dto);
    }

    @PreAuthorize("hasAuthority('UPDATE_PRODUCT')")
    @PutMapping("/{id}")
    public ProductDTO update(@PathVariable Long id, @RequestBody @Valid ProductRequestDTO request) {
        return service.update(id, request);
    }

    @PreAuthorize("hasAuthority('DELETE_PRODUCT')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('READ_PRODUCT')")
    @GetMapping("/{id}/specifications")
    public List<ProductSpecificationListDTO> findAllSpecifications(@PathVariable Long id) {
        return prodSpecService.findAll(id);
    }

    @PreAuthorize("hasAuthority('UPDATE_PRODUCT')")
    @PostMapping("/{prodId}/specifications")
    public ResponseEntity<ProductSpecificationDTO> saveSpecification(
            @PathVariable Long prodId, @RequestBody @Valid ProductSpecificationSaveDTO request) {

        ProductSpecificationDTO dto = prodSpecService.save(prodId, request);

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(dto.getId())
                .toUri();

        return ResponseEntity.created(uri).body(dto);
    }

    @PreAuthorize("hasAuthority('UPDATE_PRODUCT')")
    @PutMapping("/{prodId}/specifications/{specId}")
    public ProductSpecificationDTO update(
            @PathVariable Long prodId,
            @PathVariable Long specId,
            @RequestBody @Valid ProductSpecificationUpdateDTO request) {
        return prodSpecService.update(prodId, specId, request);
    }

    @PreAuthorize("hasAuthority('UPDATE_PRODUCT')")
    @DeleteMapping("/{prodId}/specifications/{specId}")
    public ResponseEntity<Void> deleteSpecification(@PathVariable Long prodId, @PathVariable Long specId) {
        prodSpecService.delete(prodId, specId);
        return ResponseEntity.noContent().build();
    }
}
