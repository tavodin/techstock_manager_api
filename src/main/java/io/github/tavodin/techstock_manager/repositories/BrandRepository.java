package io.github.tavodin.techstock_manager.repositories;

import io.github.tavodin.techstock_manager.entities.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BrandRepository extends JpaRepository<Brand, Long> {
}
