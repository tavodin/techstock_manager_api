package io.github.tavodin.techstock_manager.services;

import io.github.tavodin.techstock_manager.builder.ProductBuilder;
import io.github.tavodin.techstock_manager.builder.SpecificationBuilder;
import io.github.tavodin.techstock_manager.dto.ProductSpecificationDTO;
import io.github.tavodin.techstock_manager.dto.ProductSpecificationListDTO;
import io.github.tavodin.techstock_manager.dto.ProductSpecificationSaveDTO;
import io.github.tavodin.techstock_manager.dto.ProductSpecificationUpdateDTO;
import io.github.tavodin.techstock_manager.entities.*;
import io.github.tavodin.techstock_manager.enums.SpecificationType;
import io.github.tavodin.techstock_manager.exceptions.BusinessException;
import io.github.tavodin.techstock_manager.exceptions.EntityInUseException;
import io.github.tavodin.techstock_manager.exceptions.ResourceNotFoundException;
import io.github.tavodin.techstock_manager.repositories.CategoryRepository;
import io.github.tavodin.techstock_manager.repositories.ProductRepository;
import io.github.tavodin.techstock_manager.repositories.ProductSpecificationRepository;
import io.github.tavodin.techstock_manager.repositories.SpecificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


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
    private String existsSpecMsg = "The product cannot have the same specification";
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
    void shouldSaveWhenSavingWithValidData() {
        when(repository.existsByProduct_IdAndSpecification_Id(product.getId(), saveRequest.specificationId()))
                .thenReturn(false);
        when(productRepository.findById(validId)).thenReturn(Optional.of(product));
        when(specificationRepository.findById(validId)).thenReturn(Optional.of(specification));
        when(repository.save(any(ProductSpecification.class))).thenReturn(productSpecification);

        ProductSpecificationDTO actual = service.save(validId, saveRequest);

        ArgumentCaptor<ProductSpecification> captor = ArgumentCaptor.forClass(ProductSpecification.class);
        verify(repository).save(captor.capture());
        ProductSpecification captured = captor.getValue();

        assertNotNull(actual);
        assertNull(captured.getId());
        assertNull(captured.getValueBoolean());
        assertNull(captured.getValueString());
        assertEquals(saveRequest.valueNumber(), captured.getValueNumber());
        assertEquals(product, captured.getProduct());
        assertEquals(specification, captured.getSpecification());
    }

    @Test
    void shouldThrowEntityInUseExceptionWhenSavingWithExistingSpecification() {
        ProductSpecificationSaveDTO invalidRequest = new ProductSpecificationSaveDTO(
                invalidId, null, null, null);

        when(repository.existsByProduct_IdAndSpecification_Id(invalidId, invalidRequest.specificationId()))
                .thenReturn(true);

        EntityInUseException actual = assertThrows(EntityInUseException.class, () ->
                service.save(invalidId, invalidRequest));

        assertEquals(existsSpecMsg, actual.getMessage());
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenSavingWithInvalidProductId() {
        when(repository.existsByProduct_IdAndSpecification_Id(invalidId, saveRequest.specificationId()))
                .thenReturn(false);
        when(productRepository.findById(invalidId)).thenReturn(Optional.empty());

        ResourceNotFoundException actual = assertThrows(ResourceNotFoundException.class, () ->
                service.save(invalidId, saveRequest));

        assertEquals(productNotFoundMsg, actual.getMessage());
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenSavingWithInvalidSpecificationId() {
        ProductSpecificationSaveDTO invalidRequest = new ProductSpecificationSaveDTO(
                invalidId, null, null, null);

        when(repository.existsByProduct_IdAndSpecification_Id(validId, invalidRequest.specificationId()))
                .thenReturn(false);
        when(productRepository.findById(validId)).thenReturn(Optional.of(product));
        when(specificationRepository.findById(invalidRequest.specificationId()))
                .thenReturn(Optional.empty());

        ResourceNotFoundException actual = assertThrows(ResourceNotFoundException.class, () ->
                service.save(validId, invalidRequest));

        assertEquals(specificationNotFoundMsg, actual.getMessage());
    }

    @Test
    void shouldThrownBusinessExceptionWhenSavingWithNullValueString() {
        specification.setDataType(SpecificationType.STRING);
        ProductSpecificationSaveDTO invalidRequest = new ProductSpecificationSaveDTO(
                specification.getId(), null, null, null);

        when(repository.existsByProduct_IdAndSpecification_Id(validId, invalidRequest.specificationId()))
                .thenReturn(false);
        when(productRepository.findById(validId)).thenReturn(Optional.of(product));
        when(specificationRepository.findById(validId)).thenReturn(Optional.of(specification));

        BusinessException actual = assertThrows(BusinessException.class, () ->
                service.save(validId, invalidRequest));

        assertEquals(nullValueStringMsg, actual.getMessage());
    }

    @Test
    void shouldThrownBusinessExceptionWhenSavingWithNullValueNumber() {
        specification.setDataType(SpecificationType.NUMBER);
        ProductSpecificationSaveDTO invalidRequest = new ProductSpecificationSaveDTO(
                specification.getId(), null, null, null);

        when(repository.existsByProduct_IdAndSpecification_Id(validId, invalidRequest.specificationId()))
                .thenReturn(false);
        when(productRepository.findById(validId)).thenReturn(Optional.of(product));
        when(specificationRepository.findById(validId)).thenReturn(Optional.of(specification));

        BusinessException actual = assertThrows(BusinessException.class, () ->
                service.save(validId, invalidRequest));

        assertEquals(nullValueNumberMsg, actual.getMessage());
    }

    @Test
    void shouldThrownBusinessExceptionWhenSavingWithNullValueBoolean() {
        specification.setDataType(SpecificationType.BOOLEAN);
        ProductSpecificationSaveDTO invalidRequest = new ProductSpecificationSaveDTO(
                specification.getId(), null, null, null);

        when(repository.existsByProduct_IdAndSpecification_Id(validId, invalidRequest.specificationId()))
                .thenReturn(false);
        when(productRepository.findById(validId)).thenReturn(Optional.of(product));
        when(specificationRepository.findById(validId)).thenReturn(Optional.of(specification));

        BusinessException actual = assertThrows(BusinessException.class, () ->
                service.save(validId, invalidRequest));

        assertEquals(nullValueBooleanMsg, actual.getMessage());
    }

    @Test
    void shouldUpdateWhenUpdatingWithValidData() {
        when(repository.getByProductIdAndSpecificationId(product.getId(), specification.getId()))
                .thenReturn(Optional.of(productSpecification));
        when(specificationRepository.findById(specification.getId())).thenReturn(Optional.of(specification));
        when(repository.save(any(ProductSpecification.class))).thenReturn(productSpecification);

        ProductSpecificationDTO actual = service.update(validId, validId, updateRequest);

        ArgumentCaptor<ProductSpecification> captor = ArgumentCaptor.forClass(ProductSpecification.class);
        verify(repository).save(captor.capture());
        ProductSpecification captured = captor.getValue();

        assertNotNull(actual);
        assertNull(captured.getValueString());
        assertEquals(updateRequest.valueNumber(), captured.getValueNumber());
        assertNull(captured.getValueBoolean());
        assertEquals(product, captured.getProduct());
        assertEquals(specification, captured.getSpecification());
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenUpdatingWithInvalidProductOrSpecificationId() {
        when(repository.getByProductIdAndSpecificationId(invalidId, invalidId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException actual = assertThrows(ResourceNotFoundException.class, () ->
                service.update(invalidId, invalidId, updateRequest));

        assertEquals(prodSpecNotFoudMsg, actual.getMessage());
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenUpdatingWithInvalidSpecificationId() {
        when(repository.getByProductIdAndSpecificationId(validId, invalidId))
                .thenReturn(Optional.of(productSpecification));
        when(specificationRepository.findById(invalidId)).thenReturn(Optional.empty());

        ResourceNotFoundException actual = assertThrows(ResourceNotFoundException.class, () ->
                service.update(validId, invalidId, updateRequest));

        assertEquals(specificationNotFoundMsg, actual.getMessage());
    }

    @Test
    void shouldSetValueStringWhenUpdatingWithStringType() {
        specification.setDataType(SpecificationType.STRING);
        ProductSpecificationUpdateDTO validRequest = new ProductSpecificationUpdateDTO(
                "1920x1080", null, null
        );

        when(repository.getByProductIdAndSpecificationId(product.getId(), specification.getId()))
                .thenReturn(Optional.of(productSpecification));
        when(specificationRepository.findById(specification.getId())).thenReturn(Optional.of(specification));
        when(repository.save(any(ProductSpecification.class))).thenReturn(productSpecification);

        service.update(validId, validId, validRequest);

        ArgumentCaptor<ProductSpecification> captor = ArgumentCaptor.forClass(ProductSpecification.class);
        verify(repository).save(captor.capture());
        ProductSpecification captured = captor.getValue();

        assertEquals(validRequest.valueString(), captured.getValueString());
    }

    @Test
    void shouldThrowBusinessExceptionWhenUpdatingWithNullValueString() {
        specification.setDataType(SpecificationType.STRING);

        when(repository.getByProductIdAndSpecificationId(product.getId(), specification.getId()))
                .thenReturn(Optional.of(productSpecification));
        when(specificationRepository.findById(specification.getId())).thenReturn(Optional.of(specification));

        BusinessException actual = assertThrows(BusinessException.class, () ->
                service.update(validId, validId, invalidRequest));

        assertEquals(nullValueStringMsg, actual.getMessage());
    }

    @Test
    void shouldSetValueNumberWhenUpdatingWithNumberType() {
        specification.setDataType(SpecificationType.NUMBER);
        ProductSpecificationUpdateDTO validRequest = new ProductSpecificationUpdateDTO(
                null, 120.0, null
        );

        when(repository.getByProductIdAndSpecificationId(product.getId(), specification.getId()))
                .thenReturn(Optional.of(productSpecification));
        when(specificationRepository.findById(specification.getId())).thenReturn(Optional.of(specification));
        when(repository.save(any(ProductSpecification.class))).thenReturn(productSpecification);

        service.update(validId, validId, validRequest);

        ArgumentCaptor<ProductSpecification> captor = ArgumentCaptor.forClass(ProductSpecification.class);
        verify(repository).save(captor.capture());
        ProductSpecification captured = captor.getValue();

        assertEquals(validRequest.valueString(), captured.getValueString());
    }

    @Test
    void shouldThrowBusinessExceptionWhenUpdatingWithNullNumberString() {
        specification.setDataType(SpecificationType.NUMBER);
        ProductSpecificationUpdateDTO invalidRequest = new ProductSpecificationUpdateDTO(null, null, null);

        when(repository.getByProductIdAndSpecificationId(product.getId(), specification.getId()))
                .thenReturn(Optional.of(productSpecification));
        when(specificationRepository.findById(specification.getId())).thenReturn(Optional.of(specification));

        BusinessException actual = assertThrows(BusinessException.class, () ->
                service.update(validId, validId, invalidRequest));

        assertEquals(nullValueNumberMsg, actual.getMessage());
    }

    @Test
    void shouldSetValueBooleanWhenUpdatingWithBooleanType() {
        specification.setDataType(SpecificationType.BOOLEAN);
        ProductSpecificationUpdateDTO validRequest = new ProductSpecificationUpdateDTO(
                null, null, true
        );

        when(repository.getByProductIdAndSpecificationId(product.getId(), specification.getId()))
                .thenReturn(Optional.of(productSpecification));
        when(specificationRepository.findById(specification.getId())).thenReturn(Optional.of(specification));
        when(repository.save(any(ProductSpecification.class))).thenReturn(productSpecification);

        service.update(validId, validId, validRequest);

        ArgumentCaptor<ProductSpecification> captor = ArgumentCaptor.forClass(ProductSpecification.class);
        verify(repository).save(captor.capture());
        ProductSpecification captured = captor.getValue();

        assertEquals(validRequest.valueString(), captured.getValueString());
    }

    @Test
    void shouldThrowBusinessExceptionWhenUpdatingWithNullBooleanString() {
        specification.setDataType(SpecificationType.BOOLEAN);
        ProductSpecificationUpdateDTO invalidRequest = new ProductSpecificationUpdateDTO(null, null, null);

        when(repository.getByProductIdAndSpecificationId(product.getId(), specification.getId()))
                .thenReturn(Optional.of(productSpecification));
        when(specificationRepository.findById(specification.getId())).thenReturn(Optional.of(specification));

        BusinessException actual = assertThrows(BusinessException.class, () ->
                service.update(validId, validId, invalidRequest));

        assertEquals(nullValueBooleanMsg, actual.getMessage());
    }

    @Test
    void shouldDeleteWhenDeletingWithValidIds() {
        when(productRepository.findById(validId)).thenReturn(Optional.of(product));
        when(categoryRepository.findRequiredSpecificationsIdsByCategoryIds(List.of(category.getId())))
                .thenReturn(List.of(3L, 2L));
        when(repository.getByProductIdAndSpecificationId(validId, validId))
                .thenReturn(Optional.of(productSpecification));
        doNothing().when(repository).delete(productSpecification);

        service.delete(validId, validId);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenDeletingWithInvalidProductId() {
        when(productRepository.findById(invalidId)).thenReturn(Optional.empty());

        ResourceNotFoundException actual = assertThrows(ResourceNotFoundException.class, () ->
                service.delete(invalidId, validId));

        assertEquals(productNotFoundMsg, actual.getMessage());
    }

    @Test
    void shouldThrowBusinessExceptionWhenDeletingWithRequiredSpecificationId() {
        when(productRepository.findById(validId)).thenReturn(Optional.of(product));
        when(categoryRepository.findRequiredSpecificationsIdsByCategoryIds(List.of(category.getId())))
                .thenReturn(List.of(category.getId()));

        BusinessException actual = assertThrows(BusinessException.class, () ->
                service.delete(validId, validId));

        assertEquals("The specification is required and cannot be excluded", actual.getMessage());
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenDeletingWithInvalidProductOrSpecificationId() {
        when(productRepository.findById(validId)).thenReturn(Optional.of(product));
        when(categoryRepository.findRequiredSpecificationsIdsByCategoryIds(List.of(category.getId())))
                .thenReturn(List.of(category.getId()));
        when(repository.getByProductIdAndSpecificationId(validId, invalidId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException actual = assertThrows(ResourceNotFoundException.class, () ->
                service.delete(validId, invalidId));

        assertEquals(prodSpecNotFoudMsg, actual.getMessage());
    }
}
