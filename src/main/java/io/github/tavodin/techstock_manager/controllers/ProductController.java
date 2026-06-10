package io.github.tavodin.techstock_manager.controllers;

import io.github.tavodin.techstock_manager.dto.ProductDTO;
import io.github.tavodin.techstock_manager.dto.ProductRequestDTO;
import io.github.tavodin.techstock_manager.services.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public ProductDTO findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<ProductDTO> save(@RequestBody @Valid ProductRequestDTO request) {
        return ResponseEntity.status(201).body(service.save(request));
    }
}
