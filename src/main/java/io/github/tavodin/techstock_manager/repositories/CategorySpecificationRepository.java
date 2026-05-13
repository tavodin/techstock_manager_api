package io.github.tavodin.techstock_manager.repositories;

import io.github.tavodin.techstock_manager.entities.CategorySpecification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategorySpecificationRepository extends JpaRepository<CategorySpecification, Long> {
}
