package io.github.tavodin.techstock_manager.repositories;

import io.github.tavodin.techstock_manager.dto.SupplierAutocompleteDTO;
import io.github.tavodin.techstock_manager.entities.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    @Query("""
            SELECT s
            FROM Supplier s
            WHERE (:name IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%')))
            """)
    Page<Supplier> getAllByName(@Param("name") String name, Pageable pageable);

    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id);
    boolean existsByDocument(String document);
    boolean existsByDocumentAndIdNot(String sku, Long id);

    @Query("""
            SELECT new io.github.tavodin.techstock_manager.dto.SupplierAutocompleteDTO(
                s.id, s.name
            )
            FROM Supplier s
            WHERE (:name IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%')))
            """)
    List<SupplierAutocompleteDTO> getAllAutocomplete(String name, Pageable pageable);
}
