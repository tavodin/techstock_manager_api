package io.github.tavodin.techstock_manager.controllers;

import io.github.tavodin.techstock_manager.dto.BrandDTO;
import io.github.tavodin.techstock_manager.dto.BrandRequestDTO;
import io.github.tavodin.techstock_manager.services.BrandService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/brands")
public class BrandController {

    private final BrandService service;

    public BrandController(BrandService service) {
        this.service = service;
    }

    @PreAuthorize("hasAuthority('READ_BRAND')")
    @GetMapping("/{id}")
    public BrandDTO findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PreAuthorize("hasAuthority('READ_BRAND')")
    @GetMapping
    public PagedModel<BrandDTO> findAll(Pageable pageable) {
        return service.findAll(pageable);
    }

    @PreAuthorize("hasAuthority('CREATE_BRAND')")
    @PostMapping
    public ResponseEntity<BrandDTO> save(@RequestBody @Valid BrandRequestDTO request) {
        BrandDTO dto = service.save(request);

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(dto.getId())
                .toUri();

        return ResponseEntity.created(uri).body(dto);
    }


    @PreAuthorize("hasAuthority('UPDATE_BRAND')")
    @PutMapping("/{id}")
    public BrandDTO update(@PathVariable Long id, @RequestBody @Valid BrandRequestDTO request) {
        return service.update(id, request);
    }

    @PreAuthorize("hasAuthority('DELETE_BRAND')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
