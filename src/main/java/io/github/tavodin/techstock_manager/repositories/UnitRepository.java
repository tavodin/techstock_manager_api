package io.github.tavodin.techstock_manager.repositories;

import io.github.tavodin.techstock_manager.dto.UnitAutocompleteDTO;
import io.github.tavodin.techstock_manager.entities.Unit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UnitRepository extends JpaRepository<Unit, Long> {

    @Query("""
            SELECT u
            FROM Unit u
            WHERE (:name IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%')))
            """)
    Page<Unit> findAllPaged(
            @Param("name") String name,
            Pageable pageable
    );

    @Query("""
            SELECT new io.github.tavodin.techstock_manager.dto.UnitAutocompleteDTO(u.id, u.name)
            FROM Unit u
            WHERE (:name IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%')))
            """)
    List<UnitAutocompleteDTO> getUnitsByName(@Param("name") String name, Pageable pageable);
}
