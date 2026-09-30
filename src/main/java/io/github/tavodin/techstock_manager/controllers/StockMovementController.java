package io.github.tavodin.techstock_manager.controllers;

import io.github.tavodin.techstock_manager.dto.StockMovementDTO;
import io.github.tavodin.techstock_manager.enums.MovementType;
import io.github.tavodin.techstock_manager.services.StockMovementService;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.PagedModel;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/stock-movement")
public class StockMovementController {

    private final StockMovementService service;

    public StockMovementController(StockMovementService service) {
        this.service = service;
    }

    @PreAuthorize("hasAuthority('READ_STOCK_MOVEMENT')")
    @GetMapping
    public PagedModel<StockMovementDTO> findAll(
            @RequestParam(name = "productId", required = false)
            Long productId,
            @RequestParam(name = "startDate", required = false)
            LocalDate startDate,
            @RequestParam(name = "endDate", required = false)
            LocalDate endDate,
            @RequestParam(name = "type", required = false)
            MovementType type,
            Pageable pageable
    ) {
        return service.findAll(productId, startDate, endDate, type, pageable);
    }
}
