package io.github.tavodin.techstock_manager.controllers;

import io.github.tavodin.techstock_manager.dto.*;
import io.github.tavodin.techstock_manager.services.CategoryService;
import io.github.tavodin.techstock_manager.services.CategorySpecificationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService service;
    private final CategorySpecificationService catSpecService;

    public CategoryController(CategoryService service, CategorySpecificationService catSpecService) {
        this.service = service;
        this.catSpecService = catSpecService;
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
    public ResponseEntity<CategoryDTO> save(@RequestBody @Valid CategoryRequestDTO request) {
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

    @GetMapping("/{id}/specifications")
    public List<CategorySpecificationsListDTO> findAllSpecificationByCategoryId(@PathVariable Long id) {
        return service.findAllSpecificationByCategoryId(id);
    }

    @PostMapping("/specifications")
    public ResponseEntity<CategorySpecificationDTO> saveCat(@RequestBody @Valid CategorySpecificationRequestDTO request) {
        CategorySpecificationDTO dto = catSpecService.save(request);

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(dto.getId())
                .toUri();

        return ResponseEntity.created(uri).body(dto);
    }

    @PutMapping("/specifications/{id}")
    public CategorySpecificationDTO updateCatSpec(@PathVariable Long id, @RequestBody @Valid CategorySpecificationRequestDTO request) {
        return catSpecService.update(id, request);
    }

    @DeleteMapping("/specifications/{id}")
    public ResponseEntity<Void> deleteCatSpec(@PathVariable Long id) {
        catSpecService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
