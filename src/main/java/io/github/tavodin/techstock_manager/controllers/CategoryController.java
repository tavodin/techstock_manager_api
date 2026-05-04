package io.github.tavodin.techstock_manager.controllers;

import io.github.tavodin.techstock_manager.dto.CategoryDTO;
import io.github.tavodin.techstock_manager.dto.error.CategoryRequestDTO;
import io.github.tavodin.techstock_manager.services.CategoryService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService service;

    public CategoryController(CategoryService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public CategoryDTO findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @GetMapping
    public PagedModel<CategoryDTO> findAll(Pageable pageable) {
        return service.findAll(pageable);
    }

    @PostMapping
    public ResponseEntity<CategoryDTO> save(@RequestBody CategoryRequestDTO request) {
        CategoryDTO dto = service.save(request);

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(dto.getId())
                .toUri();

        return ResponseEntity.created(uri).body(dto);
    }

    @PutMapping("/{id}")
    public CategoryDTO update(@PathVariable Long id, @RequestBody @Valid CategoryRequestDTO request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
