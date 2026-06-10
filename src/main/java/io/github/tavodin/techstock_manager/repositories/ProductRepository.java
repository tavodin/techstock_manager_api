package io.github.tavodin.techstock_manager.repositories;

import io.github.tavodin.techstock_manager.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsBySku(String sku);
    boolean existsBySkuAndIdNot(String sku, Long id);
}
