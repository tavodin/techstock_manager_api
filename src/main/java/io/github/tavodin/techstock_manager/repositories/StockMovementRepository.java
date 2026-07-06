package io.github.tavodin.techstock_manager.repositories;

import io.github.tavodin.techstock_manager.dto.StockMovementDTO;
import io.github.tavodin.techstock_manager.entities.StockMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    @Query("""
    SELECT new io.github.tavodin.techstock_manager.dto.StockMovementDTO(
        sm.id,
        sm.type,
        sm.quantity,
        sm.movementDate,
        sm.reason,
        p.name,
        cb.name,
        ub.name
    )
    FROM StockMovement sm
    JOIN sm.product p
    JOIN User cb ON cb.id = sm.createdBy
    LEFT JOIN User ub ON ub.id = sm.updatedBy
    """)
    Page<StockMovementDTO> getPagedStockMovement(Pageable pageable);
}
