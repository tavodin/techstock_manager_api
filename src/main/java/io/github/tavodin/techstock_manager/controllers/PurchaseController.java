package io.github.tavodin.techstock_manager.controllers;

import io.github.tavodin.techstock_manager.dto.PurchaseDTO;
import io.github.tavodin.techstock_manager.dto.PurchaseRequestDTO;
import io.github.tavodin.techstock_manager.services.PurchaseService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/purchases")
public class PurchaseController {

    private final PurchaseService service;

    public PurchaseController(PurchaseService service) {
        this.service = service;
    }

    @PreAuthorize("hasAuthority('READ_PURCHASE')")
    @GetMapping("/{id}")
    public PurchaseDTO findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PreAuthorize("hasAuthority('READ_PURCHASE')")
    @GetMapping
    public PagedModel<PurchaseDTO> findAll(Pageable pageable) {
        return service.findAll(pageable);
    }

    @PreAuthorize("hasAuthority('CREATE_PURCHASE')")
    @PostMapping
    public ResponseEntity<PurchaseDTO> save(@Valid @RequestBody PurchaseRequestDTO request) {
        PurchaseDTO dto = service.save(request);

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(dto.getId())
                .toUri();

        return ResponseEntity.created(uri).body(dto);
    }

    @PreAuthorize("hasAuthority('COMPLETE_PURCHASE')")
    @PatchMapping("/{id}/completed")
    public PurchaseDTO completedPurchase(@PathVariable Long id) {
        return service.completedPurchase(id);
    }

    @PreAuthorize("hasAuthority('CANCEL_PURCHASE')")
    @PatchMapping("/{id}/canceled")
    public PurchaseDTO canceledPurchase(@PathVariable Long id) {
        return service.canceledPurchase(id);
    }
}
