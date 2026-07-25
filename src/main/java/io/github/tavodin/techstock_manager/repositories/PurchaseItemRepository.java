package io.github.tavodin.techstock_manager.repositories;

import io.github.tavodin.techstock_manager.entities.PurchaseItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseItemRepository extends JpaRepository<PurchaseItem, Long> {
}
