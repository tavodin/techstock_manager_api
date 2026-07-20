package io.github.tavodin.techstock_manager.repositories;

import io.github.tavodin.techstock_manager.entities.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id);
    boolean existsByDocument(String document);
    boolean existsByDocumentAndIdNot(String sku, Long id);
}
