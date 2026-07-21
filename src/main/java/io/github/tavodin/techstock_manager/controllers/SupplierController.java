package io.github.tavodin.techstock_manager.controllers;

import io.github.tavodin.techstock_manager.dto.SupplierDTO;
import io.github.tavodin.techstock_manager.dto.SupplierRequestDTO;
import io.github.tavodin.techstock_manager.services.SupplierService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/suppliers")
public class SupplierController {

    private final SupplierService service;

    public SupplierController(SupplierService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public SupplierDTO findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @GetMapping
    public PagedModel<SupplierDTO> findAll(Pageable pageable) {
        return service.findAll(pageable);
    }

    @PostMapping
    public ResponseEntity<SupplierDTO> save(@Valid @RequestBody SupplierRequestDTO request) {
        SupplierDTO dto = service.save(request);

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(dto.getId())
                .toUri();

        return ResponseEntity.created(uri).body(dto);
    }

    @PutMapping("/{id}")
    public SupplierDTO update(@PathVariable Long id, @Valid @RequestBody SupplierRequestDTO requestDTO) {
        SupplierDTO dto = service.update(id, requestDTO);
        return dto;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
