package io.github.tavodin.techstock_manager.repositories;

import io.github.tavodin.techstock_manager.dto.ProductSpecificationListDTO;
import io.github.tavodin.techstock_manager.entities.ProductSpecification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductSpecificationRepository extends JpaRepository<ProductSpecification, Long> {

    @Query("""
            SELECT ps
            FROM ProductSpecification ps
            WHERE ps.product.id = :prodId
            AND ps.specification.id = :specId
            """)
    Optional<ProductSpecification> getByProductIdAndSpecificationId(Long prodId, Long specId);

    @Query("""
            SELECT new io.github.tavodin.techstock_manager.dto.ProductSpecificationListDTO(
                ps.id, s.id, p.id, s.name, ps.valueString, ps.valueNumber, ps.valueBoolean, u.symbol
            )
            FROM ProductSpecification ps
            JOIN ps.product p
            JOIN ps.specification s
            LEFT JOIN s.unit u
            WHERE p.id = :prodId
            """)
    List<ProductSpecificationListDTO> getAllByProductId(Long prodId);

    boolean existsByProduct_IdAndSpecification_Id(Long productId, Long specificationId);
}
