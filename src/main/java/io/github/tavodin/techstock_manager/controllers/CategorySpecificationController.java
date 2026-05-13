package io.github.tavodin.techstock_manager.controllers;

import io.github.tavodin.techstock_manager.dto.CategorySpecificationDTO;
import io.github.tavodin.techstock_manager.dto.CategorySpecificationRequestDTO;
import io.github.tavodin.techstock_manager.services.CategorySpecificationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/category-specification")
public class CategorySpecificationController {

    private final CategorySpecificationService service;

    public CategorySpecificationController(CategorySpecificationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<CategorySpecificationDTO> save(@RequestBody @Valid CategorySpecificationRequestDTO request) {
        CategorySpecificationDTO dto = service.save(request);

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(dto.getId())
                .toUri();

        return ResponseEntity.created(uri).body(dto);
    }

    @PutMapping("/{id}")
    public CategorySpecificationDTO update(@PathVariable Long id, @RequestBody @Valid CategorySpecificationRequestDTO request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
