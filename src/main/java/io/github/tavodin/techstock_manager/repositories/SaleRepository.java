package io.github.tavodin.techstock_manager.repositories;

import io.github.tavodin.techstock_manager.dto.sale.SaleItemDTO;
import io.github.tavodin.techstock_manager.dto.sale.SaleListDTO;
import io.github.tavodin.techstock_manager.entities.Sale;
import io.github.tavodin.techstock_manager.enums.SaleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SaleRepository extends JpaRepository<Sale, Long> {

    @Query("""
            SELECT DISTINCT s
            FROM Sale s
            JOIN FETCH s.saleItems si
            WHERE s.id = :id
            """)
    Optional<Sale> getSaleAndItemsById(@Param("id") Long id);

    @Query("""
            SELECT new io.github.tavodin.techstock_manager.dto.sale.SaleListDTO(
                s.id, s.saleDate, s.status, s.paymentMethod, s.totalAmount, cb.name, ub.name
            )
            FROM Sale s
            JOIN User cb ON cb.id = s.createdBy
            LEFT JOIN User ub ON ub.id = s.updatedBy
            WHERE (:start IS NULL OR s.saleDate >= :start)
            AND (:end IS NULL OR s.saleDate <= :end)
            AND (:min IS NULL OR s.totalAmount >= :min)
            AND (:max IS NULL OR s.totalAmount <= :max)
            AND (:status IS NULL OR s.status = :status)
            """)
    Page<SaleListDTO> getAll(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("min") BigDecimal min,
            @Param("max") BigDecimal max,
            @Param("status") SaleStatus status,
            Pageable pageable
    );

    @Query("""
            SELECT new io.github.tavodin.techstock_manager.dto.sale.SaleItemDTO(
                p.name, si.quantity, si.unitPrice, si.subtotal
            )
            FROM SaleItem si
            JOIN si.sale s
            JOIN si.product p
            WHERE :id = s.id
            """)
    List<SaleItemDTO> getAllItemBySaleId(@Param("id") Long id);
}
