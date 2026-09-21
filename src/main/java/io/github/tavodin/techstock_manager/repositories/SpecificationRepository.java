package io.github.tavodin.techstock_manager.repositories;

import io.github.tavodin.techstock_manager.dto.CategorySpecificationsInputDTO;
import io.github.tavodin.techstock_manager.dto.SpecificationAutocompleteDTO;
import io.github.tavodin.techstock_manager.dto.SpecificationDTO;
import io.github.tavodin.techstock_manager.dto.SpecificationLoadUpdateDTO;
import io.github.tavodin.techstock_manager.entities.Specification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface SpecificationRepository extends JpaRepository<Specification, Long> {

    @EntityGraph(attributePaths = "unit")
    Optional<Specification> findById(@Param("id") Long id);

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
            WHERE (:name IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%'))) 
            """)
    Page<SpecificationDTO> findAllProjected(@Param("name") String name, Pageable pageable);

    @Query("""
            SELECT s
            FROM Specification s
            WHERE s.id IN :specificationsId
            """)
    List<Specification> getSpecificationsByIds(@Param("specificationsId") List<Long> specificationsId);

    @Query("""
            SELECT new io.github.tavodin.techstock_manager.dto.SpecificationAutocompleteDTO(s.id, s.name)
            FROM Specification s
            WHERE (:name IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%')))
            """)
    Page<SpecificationAutocompleteDTO> getSpecificationsByName(@Param("name") String name, Pageable pageable);



    @Query("""
            SELECT s
            FROM Specification s
            WHERE s.id IN :ids
            """)
    Set<Specification> getSpecificationByIds(Set<Long> ids);
}
