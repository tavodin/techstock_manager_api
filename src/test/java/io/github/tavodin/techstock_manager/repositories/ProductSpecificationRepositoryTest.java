package io.github.tavodin.techstock_manager.repositories;

import io.github.tavodin.techstock_manager.builder.ProductBuilder;
import io.github.tavodin.techstock_manager.builder.SpecificationBuilder;
import io.github.tavodin.techstock_manager.configurations.AbstractJpaTest;
import io.github.tavodin.techstock_manager.dto.ProductSpecificationListDTO;
import io.github.tavodin.techstock_manager.entities.Brand;
import io.github.tavodin.techstock_manager.entities.Product;
import io.github.tavodin.techstock_manager.entities.ProductSpecification;
import io.github.tavodin.techstock_manager.entities.Specification;
import io.github.tavodin.techstock_manager.enums.SpecificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ProductSpecificationRepositoryTest extends AbstractJpaTest {

    @Autowired
    private ProductSpecificationRepository prodSpecRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SpecificationRepository specRepository;

    @Autowired
    private BrandRepository brandRepository;

    @BeforeEach
    void setUp() {
        prodSpecRepository.deleteAll();
        specRepository.deleteAll();
        prodSpecRepository.deleteAll();
        brandRepository.deleteAll();
    }

    @Test
    void shouldFindProductWhenGetByProductIdAndSpecificationId() {
        Brand brand = new Brand();
        brand.setName("Logitech");

        brand = brandRepository.save(brand);

        Product product = ProductBuilder
                .builder()
                .withId(null)
                .build();

        product.setBrand(brand);
        product = productRepository.save(product);

        Specification specification = SpecificationBuilder
                .builder()
                .withName("Resolução")
                .withSpecificationType(SpecificationType.STRING)
                .withId(null)
                .withUnit(null)
                .build();

        specification = specRepository.save(specification);

        ProductSpecification prodSpec = new ProductSpecification();
        prodSpec.setValueString("1920x1080");
        prodSpec.setProduct(product);
        prodSpec.setSpecification(specification);

        prodSpecRepository.save(prodSpec);

        ProductSpecification findProdSpec = prodSpecRepository
                .getByProductIdAndSpecificationId(product.getId(), specification.getId()).get();

        assertNotNull(findProdSpec.getId());
        assertEquals(prodSpec.getValueString(), findProdSpec.getValueString());
        assertNull(findProdSpec.getValueNumber());
        assertNull(findProdSpec.getValueBoolean());
        assertEquals(product, findProdSpec.getProduct());
        assertEquals(specification, findProdSpec.getSpecification());
    }

    @Test
    void shouldReturnProductCollectionWhenGetAllByProductId() {
        Brand brand = new Brand();
        brand.setName("Logitech");

        brand = brandRepository.save(brand);

        Product product = ProductBuilder
                .builder()
                .withId(null)
                .build();

        product.setBrand(brand);
        product = productRepository.save(product);

        Specification firstSpec = SpecificationBuilder
                .builder()
                .withName("Resolução")
                .withSpecificationType(SpecificationType.STRING)
                .withId(null)
                .withUnit(null)
                .build();

        Specification secondSpec = SpecificationBuilder
                .builder()
                .withName("Frequência")
                .withSpecificationType(SpecificationType.NUMBER)
                .withId(null)
                .withUnit(null)
                .build();

        List<Specification> specs = specRepository.saveAll(List.of(firstSpec, secondSpec));

        ProductSpecification resolutionSpec = new ProductSpecification();
        resolutionSpec.setValueString("1920x1080");
        resolutionSpec.setProduct(product);
        resolutionSpec.setSpecification(specs.get(0));

        ProductSpecification frequencySpec = new ProductSpecification();
        frequencySpec.setValueNumber(60.0);
        frequencySpec.setProduct(product);
        frequencySpec.setSpecification(specs.get(1));

        List<ProductSpecification> savedSpecs = prodSpecRepository.saveAll(List.of(resolutionSpec, frequencySpec));
        List<ProductSpecificationListDTO> prodSpecs = prodSpecRepository.getAllByProductId(product.getId());

        ProductSpecification savedSpecResolution = savedSpecs.get(0);
        ProductSpecification savedSpecFrequency = savedSpecs.get(1);

        assertEquals(2, prodSpecs.size());

        assertEquals(savedSpecResolution.getId(), prodSpecs.get(0).getId());
        assertEquals(savedSpecResolution.getValueString(), prodSpecs.get(0).getValueString());

        assertEquals(savedSpecFrequency.getId(), prodSpecs.get(1).getId());
        assertEquals(savedSpecFrequency.getValueNumber(), prodSpecs.get(1).getValueNumber());
    }
}
