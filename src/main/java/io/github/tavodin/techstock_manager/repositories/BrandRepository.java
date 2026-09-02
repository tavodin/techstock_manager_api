package io.github.tavodin.techstock_manager.repositories;

import io.github.tavodin.techstock_manager.entities.Brand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BrandRepository extends JpaRepository<Brand, Long> {
    @Query("""
            SELECT b
            FROM Brand b
            WHERE (:name IS NULL OR LOWER(b.name) LIKE LOWER(CONCAT('%', :name, '%')))
            """)
    Page<Brand> getAll(@Param("name") String name, Pageable pageable);
}
