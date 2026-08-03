package io.github.tavodin.techstock_manager.repositories;

import io.github.tavodin.techstock_manager.entities.SaleItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaleItemRepository extends JpaRepository<SaleItem, Long> {
}
