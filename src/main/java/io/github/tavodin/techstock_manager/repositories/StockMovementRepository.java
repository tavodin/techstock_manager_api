package io.github.tavodin.techstock_manager.repositories;

import io.github.tavodin.techstock_manager.dto.StockMovementDTO;
import io.github.tavodin.techstock_manager.entities.StockMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    @Query(value = """
            SELECT new io.github.tavodin.techstock_manager.dto.StockMovementListDTO(
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
            JOIN FETCH sm.product p
            JOIN FETCH sm.createdBy cb
            LEFT FETCH sm.updatedBy ub
            """)
    Page<StockMovementDTO> getPagedStockMovement(Pageable pageable);
}
