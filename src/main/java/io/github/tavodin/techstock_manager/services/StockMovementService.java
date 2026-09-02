package io.github.tavodin.techstock_manager.services;

import io.github.tavodin.techstock_manager.assemblers.StockMovementAssembler;
import io.github.tavodin.techstock_manager.dto.StockMovementDTO;
import io.github.tavodin.techstock_manager.exceptions.BusinessException;
import io.github.tavodin.techstock_manager.repositories.StockMovementRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
    public PagedModel<StockMovementDTO> findAll(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        if(startDate == null && endDate != null) {
            throw new BusinessException("Start Date is required when the End Date is set");
        }

        LocalDateTime begin = null;
        LocalDateTime end = null;

        if(startDate != null && endDate == null) {
            begin = LocalDateTime.now();
        }

        if(startDate != null) {
            begin = LocalDateTime.of(
                    startDate.getYear(),
                    startDate.getMonth(),
                    startDate.getDayOfMonth(),
                    0,
                    0,
                    0
            );
        }

        if (endDate != null) {
            end = LocalDateTime.of(
                    endDate.getYear(),
                    endDate.getMonth(),
                    endDate.getDayOfMonth(),
                    23,
                    59,
                    59
            );
        }

        Page<StockMovementDTO> page = stockMovementRepository.getPagedStockMovement(begin, end, pageable);
        return pagedAssembler.toModel(page, assembler);
    }
}
