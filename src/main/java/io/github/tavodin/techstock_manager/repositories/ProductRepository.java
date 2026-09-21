package io.github.tavodin.techstock_manager.repositories;

import io.github.tavodin.techstock_manager.dto.product.ProductAutocompleteDTO;
import io.github.tavodin.techstock_manager.entities.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsBySku(String sku);
    boolean existsBySkuAndIdNot(String sku, Long id);

    @Query("""
            SELECT p
            FROM Product p
            WHERE (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')))
            """)
    Page<Product> getAll(@Param("name") String name, Pageable pageable);

    @Query("""
            SELECT p
            FROM Product p
            JOIN p.brand
            JOIN p.categories
            JOIN p.specifications ps
            JOIN ps.specification s
            LEFT JOIN s.unit
            WHERE p.id = :id
            """)
    Optional<Product> getProductToLoad(@Param("id") Long id);

    @Query("""
            SELECT new io.github.tavodin.techstock_manager.dto.product.ProductAutocompleteDTO(
                p.id, p.name
            )
            FROM Product p
            WHERE (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')))
            """)
    List<ProductAutocompleteDTO> getAllAutocomplete(@Param("name") String name, Pageable pageable);
}
