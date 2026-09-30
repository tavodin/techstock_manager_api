package io.github.tavodin.techstock_manager.controllers;

import io.github.tavodin.techstock_manager.dto.CategorySpecificationsInputDTO;
import io.github.tavodin.techstock_manager.dto.category.CategoryAutocompleteDTO;
import io.github.tavodin.techstock_manager.dto.category.CategoryDTO;
import io.github.tavodin.techstock_manager.dto.category.CategoryLoadDTO;
import io.github.tavodin.techstock_manager.dto.category.CategoryRequestDTO;
import io.github.tavodin.techstock_manager.dto.CategorySpecificationsListDTO;
import io.github.tavodin.techstock_manager.services.CategoryService;
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
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService service;

    public CategoryController(CategoryService service) {
        this.service = service;
    }

    @PreAuthorize("hasAuthority('READ_CATEGORY')")
    @GetMapping("/{id}")
    public CategoryDTO findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PreAuthorize("hasAuthority('READ_CATEGORY')")
    @GetMapping("/{id}/load")
    public CategoryLoadDTO loadCategoryById(@PathVariable Long id) {
        return service.loadCategoryById(id);
    }

    @PreAuthorize("hasAuthority('READ_CATEGORY')")
    @GetMapping
    public PagedModel<CategoryDTO> findAll(
            @RequestParam(name = "name", required = false) String name,
            Pageable pageable)
    {
        return service.findAll(name, pageable);
    }

    @PreAuthorize("hasAuthority('READ_CATEGORY')")
    @GetMapping("/autocomplete")
    public List<CategoryAutocompleteDTO> getCategoriesToAutocomplete(
            @RequestParam(name = "name", required = false)
            String name)
    {
        return service.getCategoriesToAutocomplete(name);
    }

    @PreAuthorize("hasAuthority('CREATE_CATEGORY')")
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

    @PreAuthorize("hasAuthority('UPDATE_CATEGORY')")
    @PutMapping("/{id}")
    public CategoryDTO update(@PathVariable Long id, @RequestBody @Valid CategoryRequestDTO request) {
        return service.update(id, request);
    }

    @PreAuthorize("hasAuthority('DELETE_CATEGORY')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('READ_CATEGORY')")
    @GetMapping("/{id}/specifications")
    public List<CategorySpecificationsListDTO> findAllSpecificationByCategoryId(@PathVariable Long id) {
        return service.findAllSpecificationByCategoryId(id);
    }

    @PreAuthorize("hasAuthority('READ_CATEGORY')")
    @GetMapping("/specifications")
    public List<CategorySpecificationsInputDTO> findAllSpecificationByCategoryIdToProduct(
            @RequestParam("ids") List<Long> ids) {
        return service.findAllSpecificationByCategoryIds(ids);
    }
}
