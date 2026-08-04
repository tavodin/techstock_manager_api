package io.github.tavodin.techstock_manager.controllers;

import io.github.tavodin.techstock_manager.dto.SaleDTO;
import io.github.tavodin.techstock_manager.dto.SaleRequestDTO;
import io.github.tavodin.techstock_manager.services.SaleService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/sales")
public class SaleController {

    private final SaleService service;

    public SaleController(SaleService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public SaleDTO findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @GetMapping
    public PagedModel<SaleDTO> findAll(Pageable pageable) {
        return service.findAll(pageable);
    }

    @PostMapping
    public ResponseEntity<SaleDTO> save(@Valid @RequestBody SaleRequestDTO request) {
        SaleDTO dto = service.save(request);

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(dto.getId())
                .toUri();

        return ResponseEntity.created(uri).body(dto);
    }

    @PatchMapping("/{id}/canceled")
    public SaleDTO canceledPurchase(@PathVariable Long id) {
        return service.canceled(id);
    }
}
