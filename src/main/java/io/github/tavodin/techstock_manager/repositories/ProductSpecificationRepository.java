package io.github.tavodin.techstock_manager.repositories;

import io.github.tavodin.techstock_manager.entities.ProductSpecification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductSpecificationRepository extends JpaRepository<ProductSpecification, Long> {
}
