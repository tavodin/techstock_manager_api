package io.github.tavodin.techstock_manager.repositories;

import io.github.tavodin.techstock_manager.dto.BrandAutocompleteDTO;
import io.github.tavodin.techstock_manager.entities.Brand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BrandRepository extends JpaRepository<Brand, Long> {
    @Query("""
            SELECT b
            FROM Brand b
            WHERE (:name IS NULL OR LOWER(b.name) LIKE LOWER(CONCAT('%', :name, '%')))
            """)
    Page<Brand> getAll(@Param("name") String name, Pageable pageable);

    @Query("""
            SELECT new io.github.tavodin.techstock_manager.dto.BrandAutocompleteDTO(b.id, b.name)
            FROM Brand b
            WHERE (:name IS NULL OR LOWER(b.name) LIKE LOWER(CONCAT('%', :name, '%')))
            """)
    List<BrandAutocompleteDTO> getBrandsByName(@Param("name") String name, Pageable pageable);
}
