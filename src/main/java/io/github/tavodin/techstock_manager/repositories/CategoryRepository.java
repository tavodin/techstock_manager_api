package io.github.tavodin.techstock_manager.repositories;

import io.github.tavodin.techstock_manager.dto.CategorySpecificationsListDTO;
import io.github.tavodin.techstock_manager.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query("""
            SELECT new io.github.tavodin.techstock_manager.dto.CategorySpecificationsListDTO(
                cs.id, s.name, cs.required
            )
            FROM CategorySpecification cs
            JOIN cs.category c
            JOIN cs.specification s
            WHERE c.id = :categoryId
            """)
    List<CategorySpecificationsListDTO> findAllSpecificationsByCategoryId(@Param("categoryId") Long categoryId);

    @Query("""
            SELECT DISTINCT cs.specification.id
            FROM CategorySpecification cs
            WHERE cs.category.id IN :categoryIds
            AND cs.required = true
            """)
    List<Long> findRequiredSpecificationsIdsByCategoryIds(@Param("categoryIds") List<Long> categoryId);
}
