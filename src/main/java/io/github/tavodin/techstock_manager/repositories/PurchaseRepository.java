package io.github.tavodin.techstock_manager.repositories;

import io.github.tavodin.techstock_manager.entities.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    @Query("""
            SELECT p
            FROM Purchase p
            JOIN FETCH p.purchaseItems
            WHERE p.id = :id
            """)
    Optional<Purchase> getPurchaseAndItems(Long id);
}
