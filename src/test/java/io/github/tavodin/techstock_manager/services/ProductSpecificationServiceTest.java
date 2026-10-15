package io.github.tavodin.techstock_manager.services;

import io.github.tavodin.techstock_manager.builder.ProductBuilder;
import io.github.tavodin.techstock_manager.builder.SpecificationBuilder;
import io.github.tavodin.techstock_manager.dto.product.ProductSpecificationListDTO;
import io.github.tavodin.techstock_manager.dto.product.ProductSpecificationSaveDTO;
import io.github.tavodin.techstock_manager.dto.product.ProductSpecificationUpdateDTO;
import io.github.tavodin.techstock_manager.entities.*;
import io.github.tavodin.techstock_manager.exceptions.BusinessException;
import io.github.tavodin.techstock_manager.exceptions.ResourceNotFoundException;
import io.github.tavodin.techstock_manager.repositories.CategoryRepository;
import io.github.tavodin.techstock_manager.repositories.ProductRepository;
import io.github.tavodin.techstock_manager.repositories.ProductSpecificationRepository;
import io.github.tavodin.techstock_manager.repositories.SpecificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class ProductSpecificationServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private SpecificationRepository specificationRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductSpecificationRepository repository;

    @InjectMocks
    private ProductSpecificationService service;

    private Long validId = 1L;
    private Long invalidId = 2L;
    private String productNotFoundMsg = "Product not found";
    private String specificationNotFoundMsg = "Specification not found";
    private String prodSpecNotFoudMsg = "Product Specification not found";
    private String nullValueStringMsg = "Value String cannot be null";
    private String nullValueNumberMsg = "Value Number cannot be null";
    private String nullValueBooleanMsg = "Value Boolean cannot be null";

    private Product product;
    private Specification specification;
    private Category category;
    private ProductSpecification productSpecification;
    private ProductSpecificationListDTO prodSpecListDTO;
    private ProductSpecificationSaveDTO saveRequest;
    private ProductSpecificationUpdateDTO updateRequest;
    private ProductSpecificationUpdateDTO invalidRequest;


    @BeforeEach
    void setUp() {
        Unit unit = new Unit(validId, null, null, "Hz", "Hertz");
        category = new Category(validId, "Monitor", null, null);

        product = ProductBuilder.builder().withId(validId).build();
        product.setCategories(Set.of(category));

        specification = SpecificationBuilder.builder()
                .withId(validId)
                .withName("Taxa de atualização")
                .withUnit(unit)
                .build();

        prodSpecListDTO = new ProductSpecificationListDTO(
                validId,
                specification.getId(),
                product.getId(),
                specification.getName(),
                null,
                60.0,
                null,
                specification.getUnit().getSymbol()
        );

        saveRequest = new ProductSpecificationSaveDTO(
                specification.getId(),
                null,
                60.0,
                null
        );

        updateRequest = new ProductSpecificationUpdateDTO(
                null,
                180.0,
                null
        );

        productSpecification = new ProductSpecification(
                validId,
                saveRequest.valueString(),
                null,
                null,
                product,
                specification
        );

        invalidRequest = new ProductSpecificationUpdateDTO(null, null, null);
    }

    @Test
    void shouldReturnProductSpecificationListWhenFindAll() {
        when(productRepository.findById(validId)).thenReturn(Optional.of(product));
        when(repository.getAllByProductId(validId)).thenReturn(List.of(prodSpecListDTO));

        List<ProductSpecificationListDTO> list = service.findAll(validId);

        ProductSpecificationListDTO actual = list.getFirst();

        assertNotNull(actual);
        assertEquals(validId, actual.getId());
        assertEquals(specification.getId(), actual.getSpecificationId());
        assertEquals(product.getId(), actual.getProductId());
        assertEquals(specification.getName(), actual.getSpecificationName());
        assertNull(actual.getValueString());
        assertEquals(prodSpecListDTO.getValueNumber(), actual.getValueNumber());
        assertNull(prodSpecListDTO.getValueBoolean());
        assertEquals(specification.getUnit().getSymbol(), actual.getUnitSymbol());
    }

    @Test
    void shouldThrownResourceNotFoundExceptionWhenFindAllWithInvalidProductId() {
        when(productRepository.findById(invalidId)).thenReturn(Optional.empty());

        ResourceNotFoundException actual = assertThrows(ResourceNotFoundException.class, () ->
                service.findAll(invalidId));

        assertEquals(productNotFoundMsg, actual.getMessage());
    }

    @Test
    void shouldDeleteWhenDeletingWithValidIds() {
        when(repository.getByProductIdAndSpecificationId(validId, validId))
                .thenReturn(Optional.of(productSpecification));
        doNothing().when(repository).delete(productSpecification);

        service.delete(validId, validId);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenDeletingWithInvalidProductOrSpecificationId() {
        when(repository.getByProductIdAndSpecificationId(validId, invalidId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException actual = assertThrows(ResourceNotFoundException.class, () ->
                service.delete(validId, invalidId));

        assertEquals(prodSpecNotFoudMsg, actual.getMessage());
    }
}
