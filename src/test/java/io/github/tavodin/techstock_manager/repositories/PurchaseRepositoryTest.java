package io.github.tavodin.techstock_manager.repositories;

import io.github.tavodin.techstock_manager.builder.ProductBuilder;
import io.github.tavodin.techstock_manager.configurations.AbstractJpaTest;
import io.github.tavodin.techstock_manager.entities.*;
import io.github.tavodin.techstock_manager.enums.PurchaseStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class PurchaseRepositoryTest extends AbstractJpaTest {

    @Autowired
    private PurchaseRepository repository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private PurchaseItemRepository purchaseItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private StockMovementRepository stockMovementRepository;

    @BeforeEach
    void setUp() {
        stockMovementRepository.deleteAll();
        purchaseItemRepository.deleteAll();
        repository.deleteAll();
        supplierRepository.deleteAll();
        productRepository.deleteAll();
        brandRepository.deleteAll();
    }

    @Test
    void shouldReturnPurchaseAndItemsWhenGetPurchaseAndItems() {
        Brand brand = new Brand();
        brand.setName("DELL");
        brand = brandRepository.save(brand);

        Product product = ProductBuilder
                .builder()
                .withId(null)
                .build();
        product.setBrand(brand);

        product = productRepository.save(product);

        Supplier supplier = new Supplier(
                "TechParts",
                "00000000000191",
                "techparts@gmail.com",
                "6137644711",
                true
        );

        supplier = supplierRepository.save(supplier);

        Purchase purchase = new Purchase();
        purchase.setStatus(PurchaseStatus.OPEN);
        purchase.setPurchaseDate(LocalDate.of(2026, 8, 5));
        purchase.setTotalAmount(BigDecimal.valueOf(2550.5));
        purchase.setSupplier(supplier);

        purchase = repository.save(purchase);

        PurchaseItem item = new PurchaseItem();
        item.setPurchase(purchase);
        item.setProduct(product);
        item.setQuantity(1);
        item.setUnitCost(product.getCostPrice());
        item.setSubtotal(product.getCostPrice());

        purchaseItemRepository.save(item);

        Purchase findPurchase = repository.getPurchaseAndItems(purchase.getId()).get();

        assertEquals(purchase.getId(), findPurchase.getId());
        assertNotNull(purchase.getPurchaseItems());
    }
}
