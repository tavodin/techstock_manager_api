package io.github.tavodin.techstock_manager.repositories;

import io.github.tavodin.techstock_manager.entities.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpecificationRepository extends JpaRepository<Specification, Long> {
}
