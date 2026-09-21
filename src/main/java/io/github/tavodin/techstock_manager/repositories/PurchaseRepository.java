package io.github.tavodin.techstock_manager.repositories;

import io.github.tavodin.techstock_manager.dto.PurchaseItemDTO;
import io.github.tavodin.techstock_manager.dto.purchase.PurchaseListDTO;
import io.github.tavodin.techstock_manager.entities.Purchase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    @Query("""
            SELECT p
            FROM Purchase p
            JOIN FETCH p.purchaseItems
            WHERE p.id = :id
            """)
    Optional<Purchase> getPurchaseAndItems(Long id);

    @Query("""
            SELECT new io.github.tavodin.techstock_manager.dto.purchase.PurchaseListDTO(
                p.id, p.purchaseDate, p.status, p.totalAmount, s.name, cb.name, ub.name
            )
            FROM Purchase p
            JOIN p.supplier s
            JOIN User cb ON cb.id = p.createdBy
            LEFT JOIN User ub ON ub.id = p.updatedBy
            WHERE (:start IS NULL OR p.purchaseDate >= :start)
            AND (:end IS NULL OR p.purchaseDate <= :end)
            AND (:min IS NULL OR p.totalAmount >= :min)
            AND (:max IS NULL OR p.totalAmount <= :max)
            AND (:supplierId IS NULL OR s.id = :supplierId)
            """)
    Page<PurchaseListDTO> getAll(
            @Param("start") LocalDate start,
            @Param("end") LocalDate end,
            @Param("min") BigDecimal min,
            @Param("max") BigDecimal max,
            @Param("supplierId") Long supplierId,
            Pageable pageable
    );

    @Query("""
            SELECT new io.github.tavodin.techstock_manager.dto.PurchaseItemDTO(
                pd.name, pi.quantity, pi.unitCost, pi.subtotal
            )
            FROM PurchaseItem pi
            JOIN pi.purchase pc
            JOIN pi.product pd
            WHERE :id = pc.id
            """)
    List<PurchaseItemDTO> getAllItemByPurchaseId(@Param("id") Long id);
}
