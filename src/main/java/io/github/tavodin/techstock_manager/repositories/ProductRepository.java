package io.github.tavodin.techstock_manager.repositories;

import io.github.tavodin.techstock_manager.dto.product.ProductAutocompleteDTO;
import io.github.tavodin.techstock_manager.entities.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsBySku(String sku);
    boolean existsBySkuAndIdNot(String sku, Long id);

    @Query("""
        SELECT DISTINCT p
        FROM Product p
        LEFT JOIN p.categories c
        LEFT JOIN p.brand b
        WHERE (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')))
        AND (:minSale IS NULL OR p.salePrice >= :minSale)
        AND (:maxSale IS NULL OR p.salePrice <= :maxSale)
        AND (:minPurchase IS NULL OR p.costPrice >= :minPurchase)
        AND (:maxPurchase IS NULL OR p.costPrice <= :maxPurchase)
        AND (:categoryIds IS NULL OR c.id IN :categoryIds)
        AND (:brandId IS NULL OR b.id = :brandId)
        AND (:active IS NULL OR p.active = :active)
        AND (:belowMinimumStock IS NULL OR p.quantityInStock < p.minimumStock)
        """)
    Page<Product> getAll(
            @Param("name") String name,
            @Param("maxSale") BigDecimal maxSale,
            @Param("minSale") BigDecimal minSale,
            @Param("maxPurchase") BigDecimal maxPurchase,
            @Param("minPurchase") BigDecimal minPurchase,
            @Param("categoryIds") Set<Long> categoryIds,
            @Param("brandId") Long brandId,
            @Param("active") Boolean active,
            @Param("belowMinimumStock") Boolean belowMinimumStock,
            Pageable pageable);

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
                p.id, p.name, p.salePrice, p.quantityInStock
            )
            FROM Product p
            WHERE (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')))
            """)
    List<ProductAutocompleteDTO> getAllAutocomplete(@Param("name") String name, Pageable pageable);
}
