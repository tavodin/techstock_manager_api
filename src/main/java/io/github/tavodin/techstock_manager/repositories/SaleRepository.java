package io.github.tavodin.techstock_manager.repositories;

import io.github.tavodin.techstock_manager.entities.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SaleRepository extends JpaRepository<Sale, Long> {

    @Query("""
            SELECT DISTINCT s
            FROM Sale s
            JOIN FETCH s.saleItems si
            WHERE s.id = :id
            """)
    Optional<Sale> getSaleAndItemsById(@Param("id") Long id);
}
