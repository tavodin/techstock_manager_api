package io.github.tavodin.techstock_manager.repositories;

import io.github.tavodin.techstock_manager.dto.SpecificationDTO;
import io.github.tavodin.techstock_manager.entities.Specification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SpecificationRepository extends JpaRepository<Specification, Long> {

    @Query("""
            SELECT new io.github.tavodin.techstock_manager.dto.SpecificationDTO(
                s.id, s.name, s.dataType, s.filterable, u.symbol, s.createdAt, s.updatedAt
            )
            FROM Specification s
            LEFT JOIN s.unit u
            WHERE s.id = :id
            """)
    Optional<SpecificationDTO> getSpecificationById(@Param("id") Long id);

    @Query("""
            SELECT new io.github.tavodin.techstock_manager.dto.SpecificationDTO(
                s.id, s.name, s.dataType, s.filterable, u.symbol, s.createdAt, s.updatedAt
            )
            FROM Specification s
            LEFT JOIN s.unit u
            """)
    Page<SpecificationDTO> findAllProjected(Pageable pageable);
}
