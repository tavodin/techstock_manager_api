package io.github.tavodin.techstock_manager.controllers;

import io.github.tavodin.techstock_manager.dto.sale.SaleDTO;
import io.github.tavodin.techstock_manager.dto.sale.SaleItemDTO;
import io.github.tavodin.techstock_manager.dto.sale.SaleListDTO;
import io.github.tavodin.techstock_manager.dto.sale.SaleRequestDTO;
import io.github.tavodin.techstock_manager.enums.PaymentMethod;
import io.github.tavodin.techstock_manager.enums.SaleStatus;
import io.github.tavodin.techstock_manager.services.SaleService;
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
import java.util.List;

@RestController
@RequestMapping("/sales")
public class SaleController {

    private final SaleService service;

    public SaleController(SaleService service) {
        this.service = service;
    }

    @PreAuthorize("hasAuthority('READ_SALE')")
    @GetMapping("/{id}")
    public SaleDTO findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PreAuthorize("hasAuthority('READ_SALE')")
    @GetMapping
    public PagedModel<SaleListDTO> findAll(
            @RequestParam(name = "start", required = false) LocalDate start,
            @RequestParam(name = "end", required = false) LocalDate end,
            @RequestParam(name = "min", required = false) BigDecimal min,
            @RequestParam(name = "max", required = false) BigDecimal max,
            @RequestParam(name = "status", required = false) SaleStatus status,
            Pageable pageable) {
        return service.findAll(start, end, min, max, status, pageable);
    }

    @PreAuthorize("hasAuthority('READ_SALE')")
    @GetMapping("/{id}/items")
    public List<SaleItemDTO> getAllItemsBySaleId(@PathVariable Long id) {
        return service.getAllItemByPurchaseId(id);
    }

    @PreAuthorize("hasAuthority('CREATE_SALE')")
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

    @PreAuthorize("hasAuthority('CANCEL_SALE')")
    @PatchMapping("/{id}/canceled")
    public SaleDTO canceledPurchase(@PathVariable Long id) {
        return service.canceled(id);
    }
}
