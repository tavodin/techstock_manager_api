package io.github.tavodin.techstock_manager.repositories;

import io.github.tavodin.techstock_manager.builder.ProductBuilder;
import io.github.tavodin.techstock_manager.configurations.AbstractJpaTest;
import io.github.tavodin.techstock_manager.entities.Brand;
import io.github.tavodin.techstock_manager.entities.Product;
import io.github.tavodin.techstock_manager.entities.Sale;
import io.github.tavodin.techstock_manager.entities.SaleItem;
import io.github.tavodin.techstock_manager.enums.SaleStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class SaleRepositoryTest extends AbstractJpaTest {

    @Autowired
    private SaleRepository repository;

    @Autowired
    private SaleItemRepository saleItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Test
    void shouldReturnSaleAndItemsWhenGetSaleAndItemsById() {
        Brand brand = new Brand();
        brand.setName("DELL");
        brand = brandRepository.save(brand);

        Product product = ProductBuilder
                .builder()
                .withId(null)
                .build();
        product.setBrand(brand);

        product = productRepository.save(product);

        Sale sale = new Sale();
        sale.setStatus(SaleStatus.COMPLETED);
        sale.setTotalAmount(BigDecimal.valueOf(2550.5));
        sale.setSaleDate(LocalDateTime.of(2026, 5, 8, 17, 30));
        sale.setPaymentMethod("Débito");

        sale = repository.save(sale);

        SaleItem item = new SaleItem();
        item.setSubtotal(product.getSalePrice());
        item.setQuantity(1);
        item.setUnitPrice(product.getSalePrice());
        item.setProduct(product);
        item.setSale(sale);

        saleItemRepository.save(item);

        Sale findSale = repository.getSaleAndItemsById(sale.getId()).get();

        assertEquals(sale.getId(), findSale.getId());
        assertNotNull(findSale.getSaleItems());
    }
}
