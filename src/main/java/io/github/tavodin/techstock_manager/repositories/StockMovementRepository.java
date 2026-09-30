package io.github.tavodin.techstock_manager.repositories;

import io.github.tavodin.techstock_manager.dto.StockMovementDTO;
import io.github.tavodin.techstock_manager.entities.StockMovement;
import io.github.tavodin.techstock_manager.enums.MovementType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
            WHERE (:startDate IS NULL OR sm.movementDate >= :startDate)
              AND (:endDate IS NULL OR sm.movementDate <= :endDate)
              AND (:productId IS NULL OR p.id = :productId)
              AND (:type IS NULL OR sm.type = :type)
    """)
    Page<StockMovementDTO> getPagedStockMovement(
            @Param("productId") Long productId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("type") MovementType type,
            Pageable pageable
    );
}
