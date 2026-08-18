package io.github.tavodin.techstock_manager.repositories;

import io.github.tavodin.techstock_manager.entities.Unit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}
