package io.github.tavodin.techstock_manager.controllers;

import io.github.tavodin.techstock_manager.dto.PurchaseItemDTO;
import io.github.tavodin.techstock_manager.dto.purchase.PurchaseDTO;
import io.github.tavodin.techstock_manager.dto.purchase.PurchaseListDTO;
import io.github.tavodin.techstock_manager.dto.purchase.PurchaseRequestDTO;
import io.github.tavodin.techstock_manager.services.PurchaseService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
    public PagedModel<PurchaseListDTO> findAll(
            @RequestParam(name = "start", required = false) LocalDate start,
            @RequestParam(name = "end", required = false) LocalDate end,
            @RequestParam(name = "min", required = false) BigDecimal min,
            @RequestParam(name = "max", required = false) BigDecimal max,
            @RequestParam(name = "supplierId", required = false) Long supplierId,
            Pageable pageable
    ) {
        return service.findAll(start, end, min, max, supplierId, pageable);
    }

    @PreAuthorize("hasAuthority('READ_PURCHASE')")
    @GetMapping("/{id}/items")
    public List<PurchaseItemDTO> getAllItemsByPurchaseId(@PathVariable Long id) {
        return service.findAllItemsByPurchaseId(id);
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
