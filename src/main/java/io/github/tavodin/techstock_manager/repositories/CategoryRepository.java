package io.github.tavodin.techstock_manager.repositories;

import io.github.tavodin.techstock_manager.dto.category.CategoryAutocompleteDTO;
import io.github.tavodin.techstock_manager.dto.category.CategoryLoadDTO;
import io.github.tavodin.techstock_manager.entities.Category;
import io.github.tavodin.techstock_manager.entities.Specification;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query("""
            SELECT c
            FROM Category c
            WHERE (:name IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%')))
            """)
    Page<Category> getAll(@Param("name") String name, Pageable pageable);

    @Query("""
            SELECT s
            FROM Category c
            JOIN c.specifications s
            WHERE c.id = :categoryId
            """)
    List<Specification> findAllSpecificationsByCategoryId(@Param("categoryId") Long categoryId);

    @Query("""
            SELECT DISTINCT cs.specification.id
            FROM CategorySpecification cs
            WHERE cs.category.id IN :categoryIds
            AND cs.required = true
            """)
    List<Long> findRequiredSpecificationsIdsByCategoryIds(@Param("categoryIds") List<Long> categoryId);

    @Query("""
            SELECT new io.github.tavodin.techstock_manager.dto.category.CategoryAutocompleteDTO(c.id, c.name)
            FROM Category c
            WHERE (:name IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%')))
            """)
    List<CategoryAutocompleteDTO> getCategoriesByName(@Param("name") String name, Pageable pageable);

    @Query("""
            SELECT c
            FROM Category c
            LEFT JOIN c.specifications
            WHERE c.id = :id
            """)
    Category loadCategoryById(@Param("id") Long id);
}
