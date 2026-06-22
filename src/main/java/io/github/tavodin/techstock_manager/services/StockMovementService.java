package io.github.tavodin.techstock_manager.services;

import io.github.tavodin.techstock_manager.assemblers.StockMovementAssembler;
import io.github.tavodin.techstock_manager.dto.StockMovementDTO;
import io.github.tavodin.techstock_manager.repositories.StockMovementRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockMovementService {

    private final StockMovementRepository stockMovementRepository;
    private final StockMovementAssembler assembler;
    private final PagedResourcesAssembler<StockMovementDTO> pagedAssembler;

    public StockMovementService(StockMovementRepository stockMovementRepository, StockMovementAssembler assembler, PagedResourcesAssembler<StockMovementDTO> pagedAssembler) {
        this.stockMovementRepository = stockMovementRepository;
        this.assembler = assembler;
        this.pagedAssembler = pagedAssembler;
    }

    @Transactional(readOnly = true)
    public PagedModel<StockMovementDTO> findAll(Pageable pageable) {
        Page<StockMovementDTO> page = stockMovementRepository.getPagedStockMovement(pageable);
        return pagedAssembler.toModel(page, assembler);
    }
}
